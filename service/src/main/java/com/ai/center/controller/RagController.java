package com.ai.center.controller;

import com.ai.center.model.Result;
import com.ai.center.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RagController {

    @Autowired
    private RagService ragService;

    public record RagRequest(Document document) {}
    @PostMapping("rag/add")
    public String rag(@RequestBody RagRequest ragRequest) {
        List<Document> documents = List.of(ragRequest.document());
        ragService.addDocuments(documents);
        return "success";
    }

     @GetMapping("/rag/query")
     public Result<List<Document>> query(@RequestParam("query") String query) {
        return Result.ok(ragService.query(query));
     }



}
