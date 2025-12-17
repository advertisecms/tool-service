package com.ai.center.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    @Autowired
    private VectorStore vectorStore;

    public void addDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }


    public List<Document> query(String query) {


        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .build()
        );

    }




}
