package com.free.aiknowledgehub.common.result;

import lombok.Data;
import lombok.Getter;

/**
 * @Description 返回值枚举
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:44
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // AI 相关
    AI_MODEL_ERROR(501, "大模型调用失败"),
    VECTOR_ERROR(502, "向量生成失败"),

    // 文件相关
    FILE_UPLOAD_ERROR(601, "文件上传失败"),
    FILE_PARSE_ERROR(602, "文件解析失败"),

    // 知识库
    KNOWLEDGE_ERROR(701, "知识库处理失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
