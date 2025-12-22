package com.ai.center.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜狐热闻API响应数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SohuHotNewsResponse {
    
    /**
     * 响应状态码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 请求是否成功
     */
    private boolean success;
    
    /**
     * 总数量
     */
    private long total;
    
    /**
     * 追踪ID
     */
    private String traceId;
    
    /**
     * 数据内容
     */
    private DataObject data;
    
    /**
     * 数据内容嵌套类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataObject {
        
        /**
         * Coze Ark数据
         */
        private CozeArk001 coze_ark_001;
        
        /**
         * Coze Ark数据嵌套类
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CozeArk001 {
            
            /**
             * 新闻列表
             */
            private List<NewsItem> list;
        }
    }
    
    /**
     * 新闻项数据模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsItem {
        
        /**
         * 新闻标题
         */
        private String title;
        
        /**
         * 新闻摘要
         */
        private String brief;
        
        /**
         * 新闻链接
         */
        private String url;
        
        /**
         * 创建NewsItem的静态方法
         */
        public static NewsItem of(String title, String brief, String url) {
            return new NewsItem(title, brief, url);
        }
    }
}