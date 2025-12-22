package com.ai.center.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ComfyUI任务状态响应模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusResponse {
    
    /**
     * 任务是否完成
     */
    private boolean finished;
    
    /**
     * 生成的图片文件名列表
     */
    private List<String> images;
    
    /**
     * 任务错误信息（如果有）
     */
    private String error;
    
    /**
     * 任务进度（百分比）
     */
    private double progress;
    
    /**
     * 任务状态描述
     */
    private String status;
    
    public static TaskStatusResponse success(List<String> images) {
        TaskStatusResponse response = new TaskStatusResponse();
        response.setFinished(true);
        response.setImages(images);
        response.setProgress(100.0);
        response.setStatus("completed");
        return response;
    }
    
    public static TaskStatusResponse running(double progress) {
        TaskStatusResponse response = new TaskStatusResponse();
        response.setFinished(false);
        response.setProgress(progress);
        response.setStatus("running");
        return response;
    }
    
    public static TaskStatusResponse error(String error) {
        TaskStatusResponse response = new TaskStatusResponse();
        response.setFinished(true);
        response.setError(error);
        response.setProgress(0.0);
        response.setStatus("error");
        return response;
    }
}