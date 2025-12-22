package com.ai.center.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.ai.center.model.AsyncTaskResponse;
import com.ai.center.model.QueueStatusResponse;
import com.ai.center.model.TaskStatusResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ComfyUI 生图工具类（Hutool-HTTP + JSON文件工作流）
 */
@Slf4j
@Component
public class ComfyUIClientUtil {
    // ComfyUI 基础地址
    @Value("${comfyUi.base-url:http://127.0.0.1:8188}")
    private String baseUrl;
    @Value("${comfyUi.workflow-path:comfyui_workflow/image.json}")
    private String imageWorkflowPath;

    @Value("${comfyui.default-timeout:60}")
    private int timeout;

    // 超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 30000;

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * 生图参数配置（仅需配置动态替换的参数，工作流由JSON文件定义）
     */

    @PostConstruct
    public void validateConfig() {
        // 验证超时配置
        if (timeout <= 0 || timeout > 600) {
            throw new IllegalArgumentException("ComfyUI超时时间必须在1-600秒之间");
        }

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("ComfyUI base-url不能为空");
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException("ComfyUI base-url必须以http://或https://开头");
        }
        log.info("ComfyUI客户端初始化完成，服务地址: {}", baseUrl);
    }



    /**
     * 加载工作流JSON文件并替换动态参数
     * @return 替换后的工作流JSON字符串
     */
    private String loadAndReplaceWorkflow(String promptStr) {


        long seedStr = IdUtil.getSnowflakeNextId();
        // 1. 读取工作流文件
        Resource resource = resourceLoader.getResource("classpath:" + "comfyui_workflow/image.json");
        StringBuilder flowStr = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                flowStr.append(line);
            }
        } catch (IOException e) {
            log.error("读取工作流文件失败: {}", imageWorkflowPath, e);
            throw new RuntimeException("读取工作流文件失败: " + e.getMessage(), e);
        }

        // 2. 动态替换工作流中的参数（核心：适配通用工作流的JSONPath路径）
        String workflowJson = StrUtil.format(flowStr.toString(), seedStr, promptStr);


        return workflowJson;
    }


    /**
     * 提交生图任务=
     * @return 任务ID（prompt_id）
     */
    public String submitTask(String promptStr) {
        try {
            // 1. 加载并替换工作流
            String workflowJson = loadAndReplaceWorkflow(promptStr);

            // 2. 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("prompt", JSON.parseObject(workflowJson));
            requestBody.put("client_id", "tool_client_" + System.currentTimeMillis());

            // 3. 提交任务
            HttpResponse response = HttpRequest.post(baseUrl + "/prompt")
                    .body(requestBody.toJSONString())
                    .contentType("application/json")
                    .timeout(DEFAULT_TIMEOUT)
                    .execute();

            if (response.getStatus() != 200) {
                throw new RuntimeException("提交任务失败，状态码: " + response.getStatus() + "，响应: " + response.body());
            }

            // 4. 解析任务ID
            JSONObject responseObj = JSON.parseObject(response.body());
            String promptId = responseObj.getString("prompt_id");
            if (StrUtil.isBlank(promptId)) {
                throw new RuntimeException("响应中未获取到prompt_id: " + response.body());
            }

            log.info("任务提交成功，prompt_id: {}", promptId);
            return promptId;
        } catch (Exception e) {
            log.error("提交任务异常", e);
            throw new RuntimeException("提交任务异常: " + e.getMessage(), e);
        }
    }

    /**
     * 查询队列状态
     * @return 队列状态信息
     */
    public QueueStatusResponse getQueueStatus() {
        try {
            HttpResponse response = HttpRequest.get(baseUrl + "/queue")
                    .timeout(DEFAULT_TIMEOUT)
                    .execute();

            if (response.getStatus() != 200) {
                log.warn("查询队列状态失败，状态码: {}", response.getStatus());
                return QueueStatusResponse.empty();
            }

            JSONObject queueObj = JSON.parseObject(response.body());
            QueueStatusResponse result = new QueueStatusResponse();
            
            // 解析正在执行的任务
            JSONObject runningObj = queueObj.getJSONObject("queue_running");
            if (runningObj != null) {
                result.setRunning(parseTaskInfos(runningObj.getJSONArray("tasks")));
            } else {
                result.setRunning(List.of());
            }
            
            // 解析等待中的任务
            JSONObject pendingObj = queueObj.getJSONObject("queue_pending");
            if (pendingObj != null) {
                result.setPending(parseTaskInfos(pendingObj.getJSONArray("tasks")));
            } else {
                result.setPending(List.of());
            }
            
            result.setTotalTasks(result.getRunning().size() + result.getPending().size());
            result.setSystemStatus(result.getTotalTasks() > 0 ? "busy" : "idle");
            result.setMaxConcurrentTasks(1); // ComfyUI默认单任务执行
            
            return result;
            
        } catch (Exception e) {
            log.error("查询队列状态异常", e);
            return QueueStatusResponse.empty();
        }
    }

    /**
     * 解析任务信息列表
     */
    private List<QueueStatusResponse.TaskInfo> parseTaskInfos(com.alibaba.fastjson2.JSONArray tasksArray) {
        List<QueueStatusResponse.TaskInfo> taskInfos = new ArrayList<>();
        if (tasksArray != null) {
            for (Object taskObj : tasksArray) {
                JSONObject task = (JSONObject) taskObj;
                QueueStatusResponse.TaskInfo taskInfo = new QueueStatusResponse.TaskInfo();
                taskInfo.setPromptId(task.getString("prompt_id"));
                taskInfo.setTaskType("image_generation");
                taskInfo.setSubmitTime(task.getLongValue("timestamp"));
                taskInfo.setStatus("queued");
                taskInfo.setProgress(0.0);
                taskInfos.add(taskInfo);
            }
        }
        return taskInfos;
    }

    /**
     * 异步提交任务
     * @param promptStr 提示词
     * @return 异步任务响应
     */
    public AsyncTaskResponse submitTaskAsync(String promptStr) {
        try {
            String promptId = submitTask(promptStr);
            if (StrUtil.isBlank(promptId)) {
                return AsyncTaskResponse.error("提交任务失败");
            }
            
            // 获取队列位置
            QueueStatusResponse queueStatus = getQueueStatus();
            int queuePosition = queueStatus.getPending().size(); // 当前在队尾
            
            return AsyncTaskResponse.success(promptId, queuePosition);
            
        } catch (Exception e) {
            log.error("异步提交任务异常", e);
            return AsyncTaskResponse.error("提交任务异常: " + e.getMessage());
        }
    }

    /**
     * 查询任务状态（改进版）
     * @param promptId 任务ID
     * @return 任务状态响应
     */
    public TaskStatusResponse queryTaskStatus(String promptId) {
        try {
            HttpResponse response = HttpRequest.get(baseUrl + "/history/" + promptId)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute();

            if (response.getStatus() != 200) {
                log.warn("查询任务状态失败，状态码: {}", response.getStatus());
                return TaskStatusResponse.running(0.0);
            }

            JSONObject historyObj = JSON.parseObject(response.body());
            if (historyObj.isEmpty() || !historyObj.containsKey(promptId)) {
                // 检查队列状态
                QueueStatusResponse queueStatus = getQueueStatus();
                boolean isRunning = queueStatus.getRunning().stream()
                    .anyMatch(task -> promptId.equals(task.getPromptId()));
                
                if (isRunning) {
                    return TaskStatusResponse.running(50.0); // 执行中
                } else {
                    boolean isPending = queueStatus.getPending().stream()
                        .anyMatch(task -> promptId.equals(task.getPromptId()));
                    if (isPending) {
                        return TaskStatusResponse.running(0.0); // 等待中
                    }
                }
                
                return TaskStatusResponse.error("任务不存在");
            }

            // 解析生成的图片列表
            JSONObject taskObj = historyObj.getJSONObject(promptId);
            JSONObject outputsObj = taskObj.getJSONObject("outputs");
            List<String> imageNames = new ArrayList<>();

            for (String key : outputsObj.keySet()) {
                JSONObject nodeObj = outputsObj.getJSONObject(key);
                if (nodeObj.containsKey("images")) {
                    for (Object imgObj : nodeObj.getJSONArray("images")) {
                        JSONObject imgJson = (JSONObject) imgObj;
                        imageNames.add(imgJson.getString("filename"));
                    }
                }
            }

            if (imageNames.isEmpty()) {
                return TaskStatusResponse.error("任务完成但未生成图片");
            }

            return TaskStatusResponse.success(imageNames);
            
        } catch (Exception e) {
            log.error("查询任务状态异常，promptId: {}", promptId, e);
            return TaskStatusResponse.error("查询任务状态异常: " + e.getMessage());
        }
    }

    /**
     * 下载图片
     * @param imageNames 图片文件名列表
     * @param saveDir 保存目录
     * @return 本地路径列表
     */
    public List<String> downloadImages(List<String> imageNames, String saveDir) {
        List<String> savedPaths = new ArrayList<>();
        FileUtil.mkdir(saveDir); // 创建目录（Hutool简化方法）

        for (String imgName : imageNames) {
            try {
                HttpResponse response = HttpRequest.get(baseUrl + "/view?filename=" + imgName + "&type=output")
                        .timeout(DEFAULT_TIMEOUT)
                        .execute();

                if (response.getStatus() != 200) {
                    log.warn("下载图片失败，状态码: {}，图片名: {}", response.getStatus(), imgName);
                    continue;
                }

                // 保存图片（Hutool IO简化）
                String savePath = StrUtil.format("{}/{}", saveDir, imgName);
                try (FileOutputStream fos = new FileOutputStream(savePath)) {
                    IoUtil.copy(response.bodyStream(), fos);
                    fos.flush();
                } catch (IOException e) {
                    FileUtil.del(savePath); // 删除可能损坏的文件
                    throw new RuntimeException("保存图片失败: " + savePath, e);
                }

                savedPaths.add(savePath);
                log.info("图片保存成功: {}", savePath);
            } catch (Exception e) {
                log.error("下载图片异常，图片名: {}", imgName, e);
            }
        }
        return savedPaths;
    }

    /**
     * 完整生图流程

     * @return 本地图片路径列表
     */
    public List<String> generateImage(String promptStr) {
        // 1. 提交任务
        String promptId = submitTask(promptStr);
        if (StrUtil.isBlank(promptId)) {
            return Collections.emptyList();
        }

        // 2. 轮询等待完成
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout * 1000L) {
            TaskStatusResponse status = queryTaskStatus(promptId);
            boolean finished = status.isFinished();

            if (finished) {
                List<String> imageNames = status.getImages();
                if (imageNames.isEmpty()) {
                    log.warn("任务完成但未生成图片");
                    return Collections.emptyList();
                }
                List<String> imageResult = new ArrayList<>();
                // 3. 返回图片URL
                for (String imgName : imageNames) {
                    imageResult.add(baseUrl + "/view?filename=" + imgName);
                }

                return imageResult;
            }

            // 等待2秒重试
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("任务等待被中断: {}", e.getMessage());
                throw new RuntimeException("任务被中断", e);
            }

            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            log.info("等待任务完成... 已耗时 {} 秒", elapsed);
        }

        throw new RuntimeException("任务超时（超时时间: " + timeout + " 秒）");
    }

}