package com.free.aiknowledgehub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.free.aiknowledgehub.entity.ChatHistoryEntity;
import com.free.aiknowledgehub.mapper.ChatHistoryMapper;
import com.free.aiknowledgehub.service.impl.ChatHistoryServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Description 对话历史服务（MySQL 权威存储，基于 MyBatis-Plus CRUD）
 * @Author: Liberty-Swine
 * @Date 2026/4/7 16:12
 */
@Service
public class ChatHistoryService extends ServiceImpl<ChatHistoryMapper, ChatHistoryEntity> implements ChatHistoryServiceImpl {

}
