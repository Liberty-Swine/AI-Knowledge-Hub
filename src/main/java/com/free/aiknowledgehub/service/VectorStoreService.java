package com.free.aiknowledgehub.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 向量存储服务
 * @Author: Liberty-Swine
 * @Date 2026/4/6 20:05
 */
@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 批量保存分片文档到 Elasticsearch VectorStore
     * 内部自动调用 EmbeddingModel 生成向量
     */
    public void saveDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }

    /**
     * 相似性检索（RAG 用）
     */
    public List<Document> similaritySearch(String query, int topK) {
        SearchRequest request = SearchRequest.builder().query(query)
                .topK(topK)
                .similarityThreshold(0.7f).build();
        return vectorStore.similaritySearch(request);
    }
}
