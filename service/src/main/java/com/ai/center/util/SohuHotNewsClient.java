package com.ai.center.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.ai.center.model.SohuHotNewsResponse;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 搜狐热闻API客户端工具类
 * 用于调用搜狐热闻接口获取最新新闻资讯
 */
@Slf4j
@Component
public class SohuHotNewsClient {
    
    // API基础地址
    @Value("${sohu.base-url:https://uis.mp.sohu.com}")
    private String baseUrl;

    @Value("${sohu.default-count:10}")
    private int defaultCount;
    // 请求路径
    private static final String API_PATH = "/blog/outer/temp/feeds/ark";
    
    // 默认User-Agent
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    // 超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 10000;
    
    /**
     * 配置验证
     */
    @PostConstruct
    public void validateConfig() {
        // 验证默认条数配置
        if (defaultCount <= 0 || defaultCount > 100) {
            throw new IllegalArgumentException("默认新闻条数必须在1-100之间");
        }

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Sohu base-url不能为空");
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Sohu base-url必须以http://或https://开头");
        }
        log.info("搜狐热闻客户端初始化完成，服务地址: {}", baseUrl);
    }

    /**
     * 获取搜狐热闻
     * @param count 获取新闻的条数（必填）
     * @param keyword 搜索关键词（可选，为空时获取所有热闻）
     * @return SohuHotNewsResponse 响应结果
     */
    public SohuHotNewsResponse getHotNews(int count, String keyword) {
        try {
            // 参数验证
            if (count <= 0 || count > 100) {
                throw new IllegalArgumentException("新闻条数必须在1-100之间");
            }
            
            // 构建请求URL
            StringBuilder urlBuilder = new StringBuilder(baseUrl).append(API_PATH)
                    .append("?count=").append(count);
            
            if (StrUtil.isNotBlank(keyword)) {
                if (keyword.length() > 100) {
                    throw new IllegalArgumentException("搜索关键词长度不能超过100字符");
                }
                urlBuilder.append("&q=").append(keyword.trim());
            }
            
            String url = urlBuilder.toString();
            log.debug("请求搜狐热闻API: {}", url);
            
            // 发送HTTP请求
            HttpResponse response = HttpRequest.get(url)
                    .header("User-Agent", USER_AGENT)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute();
            
            if (response.getStatus() != 200) {
                log.error("搜狐热闻API请求失败，状态码: {}, 响应: {}", response.getStatus(), response.body());
                throw new RuntimeException("API请求失败，状态码: " + response.getStatus());
            }
            
            // 解析响应
            String responseBody = response.body();
            if (StrUtil.isBlank(responseBody)) {
                throw new RuntimeException("响应内容为空");
            }
            
            SohuHotNewsResponse sohuResponse = JSON.parseObject(responseBody, SohuHotNewsResponse.class);
            
            // 验证响应
            if (!sohuResponse.isSuccess()) {
                log.warn("搜狐热闻API返回失败状态: {}, 消息: {}", sohuResponse.getCode(), sohuResponse.getMessage());
                throw new RuntimeException("API返回失败: " + sohuResponse.getMessage());
            }
            
            if (sohuResponse.getData() == null || sohuResponse.getData().getCoze_ark_001() == null 
                    || sohuResponse.getData().getCoze_ark_001().getList() == null) {
                log.warn("搜狐热闻API返回数据为空");
                return createEmptyResponse();
            }
            
            log.info("成功获取搜狐热闻，条数: {}", sohuResponse.getData().getCoze_ark_001().getList().size());
            return sohuResponse;
            
        } catch (Exception e) {
            log.error("获取搜狐热闻异常，关键词: {}, 条数: {}", keyword, count, e);
            throw new RuntimeException("获取搜狐热闻失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取搜狐热闻（不带关键词）
     * @return SohuHotNewsResponse 响应结果
     */
    public SohuHotNewsResponse getHotNews() {
        return getHotNews(defaultCount, null);
    }
    
    /**
     * 获取新闻列表（简化方法）
     * @param count 获取条数
     * @param keyword 关键词
     * @return 新闻列表
     */
    public List<SohuHotNewsResponse.NewsItem> getNewsList(int count, String keyword) {
        try {
            SohuHotNewsResponse response = getHotNews(count, keyword);
            if (response.isSuccess() && response.getData() != null 
                    && response.getData().getCoze_ark_001() != null) {
                return response.getData().getCoze_ark_001().getList();
            }
        } catch (Exception e) {
            log.warn("获取新闻列表失败，返回空列表", e);
        }
        return Collections.emptyList();
    }
    
    /**
     * 创建空响应
     */
    private SohuHotNewsResponse createEmptyResponse() {
        SohuHotNewsResponse response = new SohuHotNewsResponse();
        response.setCode(200);
        response.setMessage("success");
        response.setSuccess(true);
        response.setTotal(0);
        
        SohuHotNewsResponse.DataObject data = new SohuHotNewsResponse.DataObject();
        SohuHotNewsResponse.DataObject.CozeArk001 cozeArk001 = new SohuHotNewsResponse.DataObject.CozeArk001();
        cozeArk001.setList(Collections.emptyList());
        data.setCoze_ark_001(cozeArk001);
        response.setData(data);
        
        return response;
    }

    

}