package com.free.aiknowledgehub.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description 检索服务：封装向量检索与基于元数据的过滤逻辑
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Service
public class RetrievalService {

    /**
     * 向量存储（Elasticsearch VectorStore）
     */
    private final VectorStore vectorStore;

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 按知识库检索相关分片
     * <p>
     * 说明：优先使用 VectorStore 召回，再按 metadata 做过滤；如果召回不足，可增大 topK。
     * </p>
     * @param kbId 知识库ID
     * @param query 用户问题
     * @param topK  召回数量
     * @return 过滤后的候选分片
     */
    public List<Document> searchByKbId(String kbId, String query, int topK) {
        List<Document> candidates = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(Math.max(1, topK))
                        .build()
        );
        if (kbId == null || kbId.isBlank()) {
            return candidates;
        }
        List<Document> filtered = new ArrayList<>();
        for (Document d : candidates) {
            if (d == null || d.getMetadata() == null) {
                continue;
            }
            Object v = d.getMetadata().get("kbId");
            if (Objects.equals(kbId, v)) {
                filtered.add(d);
            }
        }
        // 兜底：如果过滤后为空，扩大召回再过滤一次（避免 KB 内容较少时被召回不足影响）
        if (filtered.isEmpty() && topK < 40) {
            List<Document> retry = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(40).build()
            );
            for (Document d : retry) {
                if (d == null || d.getMetadata() == null) {
                    continue;
                }
                Object v = d.getMetadata().get("kbId");
                if (Objects.equals(kbId, v)) {
                    filtered.add(d);
                }
            }
        }
        return filtered;
    }
}

