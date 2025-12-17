package com.ai.center.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RagService {

    @Autowired
    private VectorStore vectorStore;

    public void addDocuments(List<Document> documents) {
        try {
            if (documents == null || documents.isEmpty()) {
                throw new IllegalArgumentException("文档列表不能为空");
            }
            vectorStore.add(documents);
            log.info("成功添加{}个文档到向量库", documents.size());
        } catch (Exception e) {
            log.error("添加文档失败", e);
            throw new RuntimeException("添加文档失败: " + e.getMessage(), e);
        }
    }

    public List<Document> query(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("查询内容不能为空");
            }
            
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query.trim())
                            .topK(5)
                            .build()
            );
            
            log.info("查询成功，返回{}个相关文档", results.size());
            return results;
        } catch (Exception e) {
            log.error("查询失败，查询内容: {}", query, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }
}
