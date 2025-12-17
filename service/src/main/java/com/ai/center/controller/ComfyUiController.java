package com.ai.center.controller;

import cn.hutool.core.util.StrUtil;
import com.ai.center.model.Result;
import com.ai.center.util.ComfyUIClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comfyui")
public class ComfyUiController {

    @Autowired
    private ComfyUIClientUtil comfyUIClientUtil;

    @Value("${comfyui.default-timeout:60}")
    private int defaultTimeout;

    @GetMapping("/generate")
    public Result<List<String>> generate(@RequestParam("prompt") String prompt) {
        try {
            // 输入验证
            if (StrUtil.isBlank(prompt) || prompt.length() > 1000) {
                return Result.fail(400, "提示词不能为空且长度不能超过1000字符");
            }
            return Result.ok(comfyUIClientUtil.generateImage(prompt, defaultTimeout));
        } catch (Exception e) {
            return Result.fail("图像生成失败: " + e.getMessage());
        }
    }
}
