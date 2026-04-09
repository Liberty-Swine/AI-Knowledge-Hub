package com.free.aiknowledgehub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.free.aiknowledgehub.entity.ChatSessionEntity;
import com.free.aiknowledgehub.mapper.ChatSessionMapper;
import com.free.aiknowledgehub.service.impl.ChatSessionServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Description 会话服务（基于 MyBatis-Plus 通用 CRUD）
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Service
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSessionEntity> implements ChatSessionServiceImpl {
}

