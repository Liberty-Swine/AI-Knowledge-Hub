package com.free.aiknowledgehub.controller.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description 问答响应：回答文本 + 引用片段（用于溯源）
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Data
public class ChatAskResponse {
    /**
     * AI 回答内容
     */
    private String answer;
    /**
     * 引用信息（命中的分片来源）
     */
    private List<Citation> citations;

    /**
     * @Description 引用（citation）
     * @Author: Liberty-Swine
     * @Date 2026/4/9
     */
    @Data
    public static class Citation {
        /**
         * 文档ID
         */
        private String documentId;
        /**
         * 文件名
         */
        private String fileName;
        /**
         * 分片序号
         */
        private Integer chunkIndex;
        /**
         * 文件预览URL（MinIO 预签名）
         */
        private String previewUrl;
    }
}

