package com.free.aiknowledgehub.controller;

import com.free.aiknowledgehub.common.result.Result;
import com.free.aiknowledgehub.entity.ChatHistoryEntity;
import com.free.aiknowledgehub.entity.ChatSessionEntity;
import com.free.aiknowledgehub.service.ChatHistoryService;
import com.free.aiknowledgehub.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @Description 对话会话管理接口：创建会话、按知识库列出会话
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@RestController
@RequestMapping("/api/chat/sessions")
public class ChatSessionController {

    /**
     * 会话服务
     */
    private final ChatSessionService chatSessionService;
    /**
     * 历史记录服务（MySQL）
     */
    private final ChatHistoryService chatHistoryService;

    public ChatSessionController(ChatSessionService chatSessionService, ChatHistoryService chatHistoryService) {
        this.chatSessionService = chatSessionService;
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * 创建会话（绑定知识库）
     * @param req 请求体（kbId/title）
     * @return 创建后的会话
     */
    @PostMapping
    public Result<ChatSessionEntity> create(@RequestBody ChatSessionEntity req) {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setKbId(req.getKbId());
        session.setTitle(req.getTitle());
        Date now = new Date();
        session.setCreateTime(now);
        session.setLastActiveTime(now);
        chatSessionService.save(session);
        return Result.success(session);
    }

    /**
     * 按知识库列出会话（最近活跃优先）
     * @param kbId 知识库ID
     * @return 会话列表
     */
    @GetMapping
    public Result<List<ChatSessionEntity>> list(@RequestParam("kbId") String kbId) {
        List<ChatSessionEntity> list = chatSessionService.lambdaQuery()
                .eq(ChatSessionEntity::getKbId, kbId)
                .orderByDesc(ChatSessionEntity::getLastActiveTime)
                .list();
        return Result.success(list);
    }

    /**
     * 获取会话详情
     * @param sessionId 会话ID
     * @return 会话信息
     */
    @GetMapping("/{sessionId}")
    public Result<ChatSessionEntity> get(@PathVariable("sessionId") String sessionId) {
        return Result.success(chatSessionService.getById(sessionId));
    }

    /**
     * 查询某个会话的历史消息列表（默认按时间正序）
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    @GetMapping("/{sessionId}/messages")
    public Result<List<ChatHistoryEntity>> messages(@PathVariable("sessionId") String sessionId) {
        List<ChatHistoryEntity> list = chatHistoryService.lambdaQuery()
                .eq(ChatHistoryEntity::getSessionId, sessionId)
                .orderByAsc(ChatHistoryEntity::getCreateTime)
                .list();
        return Result.success(list);
    }
}

