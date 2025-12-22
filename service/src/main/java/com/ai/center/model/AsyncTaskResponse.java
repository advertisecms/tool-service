package com.ai.center.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异步任务提交响应模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskResponse {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务提交状态
     */
    private String status;
    
    /**
     * 预估完成时间（秒）
     */
    private int estimatedTime;
    
    /**
     * 队列位置
     */
    private int queuePosition;
    
    /**
     * 任务类型
     */
    private String taskType;
    
    /**
     * 消息
     */
    private String message;
    
    public static AsyncTaskResponse success(String taskId, int queuePosition) {
        AsyncTaskResponse response = new AsyncTaskResponse();
        response.setTaskId(taskId);
        response.setStatus("submitted");
        response.setQueuePosition(queuePosition);
        response.setTaskType("image_generation");
        response.setMessage("任务提交成功");
        response.setEstimatedTime(60); // 默认预估60秒
        return response;
    }
    
    public static AsyncTaskResponse error(String message) {
        AsyncTaskResponse response = new AsyncTaskResponse();
        response.setStatus("error");
        response.setMessage(message);
        return response;
    }
}