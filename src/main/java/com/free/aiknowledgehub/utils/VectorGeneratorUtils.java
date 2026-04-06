package com.free.aiknowledgehub.utils;

import jakarta.annotation.Resource;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 向量生成工具
 * @Author: Liberty-Swine
 * @Date 2026/4/6 19:46
 */
@Component
public class VectorGeneratorUtils {
    @Resource
    private OllamaEmbeddingModel embeddingModel;

    /**
     * 单文本生成向量
     */
    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    /**
     * 批量生成向量（文本分片 → 批量转向量）
     */
    public List<float[]> embedBatch(List<String> textList) {
        return embeddingModel.embed(textList);
    }
}
