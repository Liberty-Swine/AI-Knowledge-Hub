package com.free.aiknowledgehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @Author: Liberty-Swine
 * @Date 2026/4/7 16:02
 */
@Data
@TableName("chat_history")
public class ChatHistoryEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String sessionId;
    private String userQuestion;
    private String aiAnswer;
    private Date createTime;
}
