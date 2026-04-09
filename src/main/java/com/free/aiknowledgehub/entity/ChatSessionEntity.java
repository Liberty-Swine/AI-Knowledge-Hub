package com.free.aiknowledgehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Description 对话会话实体：用于组织对话历史与绑定知识库
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Data
@TableName("chat_session")
public class ChatSessionEntity {
    /**
     * 会话ID（UUID）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 关联知识库ID（kbId）
     */
    private String kbId;
    /**
     * 会话标题（可选）
     */
    private String title;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 最后活跃时间（用于排序/最近会话）
     */
    private Date lastActiveTime;
}

