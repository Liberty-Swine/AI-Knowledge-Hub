package com.free.aiknowledgehub.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Description 向量存储服务
 * @Author: Liberty-Swine
 * @Date 2026/4/6 20:05
 */
@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    /**
     * 每批次处理的分片数（根据模型/ES性能调整）
     */
    private static final int BATCH_SIZE = 20;

    /**
     * 向量化+存储超时时间
     */
    private static final long TIMEOUT_SECONDS = 30;

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
     * 批量写入到es
     * @param documents
     */
    public void saveDocumentByBatch(List<Document> documents){
        writeBatchToEs(documents, BATCH_SIZE);
    }

    /**
     * 批量保存分片文档到 Elasticsearch VectorStore
     * @param batchDocs
     * @param batchSize
     */
    public void writeBatchToEs(List<Document> batchDocs,int batchSize) {
        if(batchDocs.size()<=batchSize){
            writeBatchToEs(batchDocs);
        }else {
            List<Document> insertList=new ArrayList<>();
            for (int i = 0; i < batchDocs.size(); i++) {
                insertList.add(batchDocs.get(i));
                if (batchDocs.size() >= BATCH_SIZE) {
                    this.writeBatchToEs(insertList);
                    batchDocs.clear();
                }
            }
            // 处理最后一批剩余分片
            this.writeBatchToEs(insertList);
        }
    }

    /**
     * 批量写入到es
     * @param batchDocs
     */
    public void writeBatchToEs(List<Document> batchDocs) {
        try{
            // 超时控制：30秒超时
            CompletableFuture.runAsync(() -> vectorStore.add(batchDocs))
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }catch (TimeoutException e){
            throw new RuntimeException("向量化+存储超时：" + e.getMessage());
        }catch (Exception e){
            // 重试机制（最多3次）
            int retryCount = 0;
            while (retryCount < 3) {
                try {
                    // 指数退避
                    Thread.sleep(1000 * (retryCount + 1));
                    vectorStore.add(batchDocs);
                    return;
                } catch (Exception retryE) {
                    retryCount++;
                    if (retryCount >= 3) {
                        throw new RuntimeException("ES批量写入失败，重试3次仍失败", retryE);
                    }
                }
            }
        }
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
