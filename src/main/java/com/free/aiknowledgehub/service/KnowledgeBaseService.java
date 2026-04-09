package com.free.aiknowledgehub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.free.aiknowledgehub.entity.KnowledgeBaseEntity;
import com.free.aiknowledgehub.mapper.KnowledgeBaseMapper;
import com.free.aiknowledgehub.service.impl.KnowledgeBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Description 知识库服务（基于 MyBatis-Plus 通用 CRUD）
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Service
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBaseEntity> implements KnowledgeBaseServiceImpl {
}

