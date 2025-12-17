package com.ai.center.controller;

import com.ai.center.model.Result;
import com.ai.center.util.ComfyUIClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/generate")
    public Result<List<String>> add(@RequestParam("prompt") String prompt) {
        return Result.ok(comfyUIClientUtil.generateImage(prompt, 60000));
    }
}
