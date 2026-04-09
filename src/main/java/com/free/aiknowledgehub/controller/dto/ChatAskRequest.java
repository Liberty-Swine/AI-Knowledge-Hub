package com.free.aiknowledgehub.controller.dto;

import lombok.Data;

/**
 * @Description 问答请求：绑定知识库与会话
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Data
public class ChatAskRequest {
    /**
     * 知识库ID
     */
    private String kbId;
    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 用户问题
     */
    private String question;
    /**
     * 召回 topK（可选）
     */
    private Integer topK;
}

