package com.free.aiknowledgehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Description 知识库实体（一个知识库包含多个文档与向量分片）
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBaseEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    /**
     * 知识库ID（UUID）
     */
    private String id;
    /**
     * 知识库名称
     */
    private String name;
    /**
     * 知识库描述
     */
    private String description;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}

