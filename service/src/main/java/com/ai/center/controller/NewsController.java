package com.ai.center.controller;

import cn.hutool.core.util.StrUtil;
import com.ai.center.model.Result;
import com.ai.center.model.SohuHotNewsResponse;
import com.ai.center.util.SohuHotNewsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 新闻资讯控制器
 * 提供搜狐热闻获取相关的API接口
 */
@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private SohuHotNewsClient sohuHotNewsClient;


    /**
     * 获取热闻列表（支持关键词搜索）
     */
    @GetMapping("/hot")
    public Result<SohuHotNewsResponse> getHotNews(
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            // 参数验证
            if (count <= 0 || count > 50) {
                return Result.fail(400, "新闻条数必须在1-50之间");
            }
            
            if (StrUtil.isNotBlank(keyword) && keyword.length() > 100) {
                return Result.fail(400, "搜索关键词长度不能超过100字符");
            }
            
            SohuHotNewsResponse response = sohuHotNewsClient.getHotNews(count, keyword);
            return Result.ok(response);
            
        } catch (Exception e) {
            return Result.fail("获取热闻失败: " + e.getMessage());
        }
    }

    /**
     * 获取热闻列表（仅返回新闻列表，简化版）
     */
    @GetMapping("/list")
    public Result<List<SohuHotNewsResponse.NewsItem>> getNewsList(
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            // 参数验证
            if (count <= 0 || count > 50) {
                return Result.fail(400, "新闻条数必须在1-50之间");
            }
            
            if (StrUtil.isNotBlank(keyword) && keyword.length() > 100) {
                return Result.fail(400, "搜索关键词长度不能超过100字符");
            }
            
            List<SohuHotNewsResponse.NewsItem> newsList = sohuHotNewsClient.getNewsList(count, keyword);
            return Result.ok(newsList);
            
        } catch (Exception e) {
            return Result.fail("获取新闻列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新热闻（默认条数）
     */
    @GetMapping("/latest")
    public Result<SohuHotNewsResponse> getLatestNews() {
        try {
            SohuHotNewsResponse response = sohuHotNewsClient.getHotNews();
            return Result.ok(response);
            
        } catch (Exception e) {
            return Result.fail("获取最新热闻失败: " + e.getMessage());
        }
    }

    /**
     * 按分类获取热闻
     */
    @GetMapping("/category/{category}")
    public Result<SohuHotNewsResponse> getNewsByCategory(
            @PathVariable("category") String category,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        try {
            // 参数验证
            if (StrUtil.isBlank(category)) {
                return Result.fail(400, "分类名称不能为空");
            }
            
            if (category.length() > 50) {
                return Result.fail(400, "分类名称长度不能超过50字符");
            }
            
            if (count <= 0 || count > 50) {
                return Result.fail(400, "新闻条数必须在1-50之间");
            }
            
            // 使用分类作为关键词进行搜索
            SohuHotNewsResponse response = sohuHotNewsClient.getHotNews(count, category);
            return Result.ok(response);
            
        } catch (Exception e) {
            return Result.fail("获取分类热闻失败: " + e.getMessage());
        }
    }

    /**
     * 搜索新闻
     */
    @GetMapping("/search")
    public Result<SohuHotNewsResponse> searchNews(
            @RequestParam("q") String query,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        try {
            // 参数验证
            if (StrUtil.isBlank(query)) {
                return Result.fail(400, "搜索关键词不能为空");
            }
            
            if (query.length() > 100) {
                return Result.fail(400, "搜索关键词长度不能超过100字符");
            }
            
            if (count <= 0 || count > 50) {
                return Result.fail(400, "新闻条数必须在1-50之间");
            }
            
            SohuHotNewsResponse response = sohuHotNewsClient.getHotNews(count, query.trim());
            return Result.ok(response);
            
        } catch (Exception e) {
            return Result.fail("搜索新闻失败: " + e.getMessage());
        }
    }
}