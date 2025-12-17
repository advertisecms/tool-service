package com.ai.center.controller;

import com.ai.center.model.Result;
import com.ai.center.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {

    @Autowired
    private RagService ragService;

    public record RagRequest(Document document) {}
    
    @PostMapping("/add")
    public Result<String> addDocument(@RequestBody RagRequest ragRequest) {
        try {
            List<Document> documents = List.of(ragRequest.document());
            ragService.addDocuments(documents);
            return Result.ok("文档添加成功");
        } catch (Exception e) {
            return Result.fail("添加文档失败: " + e.getMessage());
        }
    }

    @GetMapping("/query")
    public Result<List<Document>> query(@RequestParam("query") String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return Result.fail(400, "查询内容不能为空");
            }
            return Result.ok(ragService.query(query));
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }
}
