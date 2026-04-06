package com.free.aiknowledgehub.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 文本分片工具 按 段落 → 句子 → 固定长度 分层切割
 * @Author: Liberty-Swine
 * @Date 2026/4/6 19:42
 */
@Component
public class TextSplitterUtils {

    // 分片最大长度（根据模型调整，一般 500~1000 字符）
    private static final int MAX_CHUNK_LENGTH = 800;
    // 分片重叠长度（防止语义断裂）
    private static final int OVERLAP_LENGTH = 100;

    // 按 token 分片，默认最大 512 token，重叠 50 token
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    /**
     * 文本分片，使用textSplitter
     * @param content
     * @param documentId
     * @return
     */
    public List<Document> split(String content, String documentId) {
        // 构建 Spring AI Document（含元数据）
        Document document = Document.builder()
                .text(content)
                //文件id唯一标识
                .metadata("documentId",documentId)
                .build();
        // 分片
        return textSplitter.split(List.of(document));
    }



    /**
     * 长文本智能分片
     */
    public List<String> splitText(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return chunks;
        }

        // 1. 先按换行/段落分割
        String[] paragraphs = content.split("\\n+");

        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            para = para.trim();
            if (para.isBlank()) {
                continue;
            }

            // 2. 段落太长 → 按句子分割
            if (para.length() > MAX_CHUNK_LENGTH) {
                List<String> sentences = splitSentences(para);
                for (String sentence : sentences) {
                    processSentence(chunks, currentChunk, sentence);
                }
            } else {
                // 段落直接加入
                processSentence(chunks, currentChunk, para);
            }
        }

        // 最后一段加入结果
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 按句子分割（支持 。！？；）
     */
    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        String[] parts = text.split("(?<=[。！？；])");
        for (String part : parts) {
            if (!part.isBlank()) {
                sentences.add(part.trim());
            }
        }
        return sentences;
    }

    /**
     * 把句子加入当前分片，满了就保存并开启新分片
     */
    private void processSentence(List<String> chunks, StringBuilder currentChunk, String sentence) {
        // 加入后不超限 → 直接加
        if (currentChunk.length() + sentence.length() + 1 <= MAX_CHUNK_LENGTH) {
            if (!currentChunk.isEmpty()) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);
        } else {
            // 超限 → 保存当前分片
            if (!currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());

                // 重叠部分（保持上下文）
                int overlapStart = Math.max(0, currentChunk.length() - OVERLAP_LENGTH);
                currentChunk = new StringBuilder(currentChunk.substring(overlapStart));
            }
            // 新句子加入
            currentChunk.append(sentence);
        }
    }
}
