package com.free.aiknowledgehub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Description 对话上下文缓存服务：用 Redis 保存 session 最近 N 轮对话，提供滑动窗口上下文
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Service
public class ChatMemoryService {

    /**
     * Redis 操作模板（String）
     */
    private final StringRedisTemplate redisTemplate;
    /**
     * JSON 序列化
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 每个 session 保留最近 N 轮问答
     */
    private static final int MAX_TURNS = 6;
    /**
     * 会话上下文默认过期时间（避免 Redis 无限增长）
     */
    private static final Duration TTL = Duration.ofHours(24);

    public ChatMemoryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取会话历史（最近 N 轮）
     * @param sessionId 会话ID
     * @return 最近 N 轮对话
     */
    public List<ChatTurn> getRecentTurns(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Collections.emptyList();
        }
        String key = key(sessionId);
        List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<ChatTurn> turns = new ArrayList<>();
        for (String s : raw) {
            try {
                turns.add(objectMapper.readValue(s, ChatTurn.class));
            } catch (Exception ignored) {
            }
        }
        return turns;
    }

    /**
     * 追加一轮对话，并裁剪到最近 N 轮（滑动窗口）
     * @param sessionId 会话ID
     * @param turn 一轮问答
     */
    public void appendTurn(String sessionId, ChatTurn turn) {
        if (sessionId == null || sessionId.isBlank() || turn == null) {
            return;
        }
        String key = key(sessionId);
        try {
            String json = objectMapper.writeValueAsString(turn);
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.opsForList().trim(key, Math.max(0, -MAX_TURNS), -1);
            redisTemplate.expire(key, TTL);
        } catch (JsonProcessingException ignored) {
        }
    }

    private String key(String sessionId) {
        return "chat:session:" + sessionId + ":turns";
    }

    /**
     * @Description 对话轮次（用户问题+AI回答）
     * @param userQuestion 用户问题
     * @param aiAnswer AI回答
     */
    public record ChatTurn(String userQuestion, String aiAnswer) {
    }
}

