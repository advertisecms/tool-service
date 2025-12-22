package com.ai.center.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ComfyUI队列状态响应模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    
    /**
     * 正在执行的任务
     */
    private List<TaskInfo> running;
    
    /**
     * 等待中的任务
     */
    private List<TaskInfo> pending;
    
    /**
     * 队列中任务总数
     */
    private int totalTasks;
    
    /**
     * 系统状态
     */
    private String systemStatus;
    
    /**
     * 最大并发任务数
     */
    private int maxConcurrentTasks;
    
    /**
     * 任务信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskInfo {
        /**
         * 任务ID
         */
        private String promptId;
        
        /**
         * 任务类型
         */
        private String taskType;
        
        /**
         * 提交时间戳
         */
        private long submitTime;
        
        /**
         * 开始执行时间戳
         */
        private long startTime;
        
        /**
         * 进度百分比
         */
        private double progress;
        
        /**
         * 任务状态
         */
        private String status;
    }
    
    public static QueueStatusResponse empty() {
        QueueStatusResponse response = new QueueStatusResponse();
        response.setRunning(List.of());
        response.setPending(List.of());
        response.setTotalTasks(0);
        response.setSystemStatus("idle");
        response.setMaxConcurrentTasks(1);
        return response;
    }
}