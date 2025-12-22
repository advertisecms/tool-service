package com.ai.center.controller;

import cn.hutool.core.util.StrUtil;
import com.ai.center.model.AsyncTaskResponse;
import com.ai.center.model.QueueStatusResponse;
import com.ai.center.model.Result;
import com.ai.center.model.TaskStatusResponse;
import com.ai.center.util.ComfyUIClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comfyui")
public class ComfyUiController {

    @Autowired
    private ComfyUIClientUtil comfyUIClientUtil;


    @GetMapping("/generate")
    public Result<List<String>> generate(@RequestParam("prompt") String prompt) {
        try {
            // 输入验证
            if (StrUtil.isBlank(prompt) || prompt.length() > 1000) {
                return Result.fail(400, "提示词不能为空且长度不能超过1000字符");
            }
            return Result.ok(comfyUIClientUtil.generateImage(prompt));
        } catch (Exception e) {
            return Result.fail("图像生成失败: " + e.getMessage());
        }
    }

    /**
     * 异步提交图像生成任务
     */
    @PostMapping("/submit")
    public Result<AsyncTaskResponse> submitAsync(@RequestParam("prompt") String prompt) {
        try {
            // 输入验证
            if (StrUtil.isBlank(prompt) || prompt.length() > 1000) {
                return Result.fail(400, "提示词不能为空且长度不能超过1000字符");
            }
            AsyncTaskResponse response = comfyUIClientUtil.submitTaskAsync(prompt);
            return Result.ok(response);
        } catch (Exception e) {
            return Result.fail("异步提交任务失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/status/{taskId}")
    public Result<TaskStatusResponse> getTaskStatus(@PathVariable("taskId") String taskId) {
        try {
            if (StrUtil.isBlank(taskId)) {
                return Result.fail(400, "任务ID不能为空");
            }
            TaskStatusResponse status = comfyUIClientUtil.queryTaskStatus(taskId);
            return Result.ok(status);
        } catch (Exception e) {
            return Result.fail("查询任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 查询队列状态
     */
    @GetMapping("/queue")
    public Result<QueueStatusResponse> getQueueStatus() {
        try {
            QueueStatusResponse queueStatus = comfyUIClientUtil.getQueueStatus();
            return Result.ok(queueStatus);
        } catch (Exception e) {
            return Result.fail("查询队列状态失败: " + e.getMessage());
        }
    }
}
