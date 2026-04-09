package com.free.aiknowledgehub.controller;

import com.free.aiknowledgehub.common.result.Result;
import com.free.aiknowledgehub.controller.dto.ChatAskRequest;
import com.free.aiknowledgehub.controller.dto.ChatAskResponse;
import com.free.aiknowledgehub.entity.ChatHistoryEntity;
import com.free.aiknowledgehub.entity.ChatSessionEntity;
import com.free.aiknowledgehub.service.ChatHistoryService;
import com.free.aiknowledgehub.service.ChatMemoryService;
import com.free.aiknowledgehub.service.ChatSessionService;
import com.free.aiknowledgehub.service.RetrievalService;
import com.free.aiknowledgehub.utils.MinioUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description AI对话
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:13
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    /**
     * Ollama 聊天模型
     */
    private final OllamaChatModel ollamaChatModel;

    /**
     * RAG 检索服务
     */
    private final RetrievalService retrievalService;

    /**
     * 会话服务
     */
    private final ChatSessionService chatSessionService;

    /**
     * 历史记录服务（MySQL）
     */
    private final ChatHistoryService chatHistoryService;

    /**
     * 会话上下文缓存（Redis）
     */
    private final ChatMemoryService chatMemoryService;

    /**
     * MinIO 工具（用于溯源预览URL）
     */
    private MinioUtils minioUtils;


    public ChatController(OllamaChatModel ollamaChatModel,
                          RetrievalService retrievalService,
                          ChatSessionService chatSessionService,
                          ChatHistoryService chatHistoryService,
                          ChatMemoryService chatMemoryService,
                          MinioUtils minioUtils) {
        this.ollamaChatModel = ollamaChatModel;
        this.retrievalService = retrievalService;
        this.chatSessionService = chatSessionService;
        this.chatHistoryService = chatHistoryService;
        this.chatMemoryService = chatMemoryService;
//        this.minioUtils = minioUtils;
    }

    /**
     * 基础问答测试
     * @param question
     * @return
     */
    @GetMapping("/test/{question}")
    public Result chatTest(@PathVariable(value="question") String question){
        String call = ollamaChatModel.call(question);
        return Result.success(call);
    }

    /**
     * RAG 问答（推荐）：支持 kbId + sessionId，返回 citations，并写入 MySQL 历史与 Redis 上下文
     * @param req 请求体（kbId/sessionId/question/topK）
     * @return 回答与引用信息
     */
    @PostMapping("/ask")
    public Result<ChatAskResponse> ask(@RequestBody ChatAskRequest req) {
        String normalizedQuestion = normalizeQuestion(req.getQuestion());
        int topK = req.getTopK() == null ? 10 : req.getTopK();

        // 1) 会话校验与更新时间
        ChatSessionEntity session = chatSessionService.getById(req.getSessionId());
        if (session != null) {
            ChatSessionEntity upd = new ChatSessionEntity();
            upd.setId(session.getId());
            upd.setLastActiveTime(new Date());
            chatSessionService.updateById(upd);
        }

        // 2) 取 Redis 最近 N 轮上下文（可为空）
        List<ChatMemoryService.ChatTurn> turns = chatMemoryService.getRecentTurns(req.getSessionId());

        // 3) RAG 检索（按 kbId 过滤）
        List<Document> docs = retrievalService.searchByKbId(req.getKbId(), normalizedQuestion, topK);

        // 4) 构建上下文与提示词
        String ragContext = buildContext(docs, 4, 8000);
        String memoryContext = buildMemoryContext(turns, 3000);

        String userPrompt = """
            你是一个智能助手。请根据以下参考信息回答问题。
            如果参考信息中没有答案，请直接说你不知道。

            历史对话：
            %s

            参考信息：
            %s

            问题：
            %s
            """.formatted(memoryContext, ragContext, normalizedQuestion);

        ChatClient chatClient = ChatClient.builder(ollamaChatModel).build();
        String answer = chatClient.prompt().user(userPrompt).call().content();

        // 5) 组装 citations（用于溯源）
        List<ChatAskResponse.Citation> citations = buildCitations(docs, 4);
        ChatAskResponse resp = new ChatAskResponse();
        resp.setAnswer(answer);
        resp.setCitations(citations);

        // 6) 写入 MySQL 历史记录
        ChatHistoryEntity history = new ChatHistoryEntity();
        history.setKbId(req.getKbId());
        history.setSessionId(req.getSessionId());
        history.setUserQuestion(normalizedQuestion);
        history.setAiAnswer(answer);
        history.setCreateTime(new Date());
        chatHistoryService.save(history);

        // 7) 更新 Redis 会话上下文（滑动窗口）
        chatMemoryService.appendTurn(req.getSessionId(), new ChatMemoryService.ChatTurn(normalizedQuestion, answer));

        return Result.success(resp);
    }

    private static String normalizeQuestion(String question) {
        if (question == null) {
            return "";
        }
        String q = question.trim();
        try {
            // PathVariable 通常已解码，但这里做一次容错（有些客户端会重复/异常编码）。
            q = URLDecoder.decode(q, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        return q.replaceAll("\\s+", " ");
    }

    /**
     * 构建“历史对话”上下文（滑动窗口），控制最大长度避免 Prompt 过长
     * @param turns 最近 N 轮
     * @param maxChars 最大字符数
     * @return 拼接后的上下文
     */
    private static String buildMemoryContext(List<ChatMemoryService.ChatTurn> turns, int maxChars) {
        if (turns == null || turns.isEmpty()) {
            return "";
        }
        String joined = turns.stream()
                .map(t -> "用户：" + t.userQuestion() + "\n助手：" + t.aiAnswer())
                .collect(Collectors.joining("\n\n"));
        if (joined.length() <= maxChars) {
            return joined;
        }
        return joined.substring(0, Math.max(0, maxChars));
    }

    private static String buildContext(List<Document> docs, int maxDocs, int maxChars) {
        Set<String> uniqueChunks = new LinkedHashSet<>();
        for (Document d : docs) {
            if (d == null || d.getText() == null) {
                continue;
            }
            String text = d.getText().trim();
            if (!text.isEmpty()) {
                uniqueChunks.add(text);
            }
            if (uniqueChunks.size() >= maxDocs) {
                break;
            }
        }

        String joined = uniqueChunks.stream()
                .map(t -> "-----\n" + t)
                .collect(Collectors.joining("\n\n"));

        if (joined.length() <= maxChars) {
            return joined;
        }
        return joined.substring(0, Math.max(0, maxChars));
    }

    /**
     * 构建 citations（引用片段来源），用于前端展示“答案来源”与跳转预览
     * @param docs 命中的文档分片
     * @param maxDocs 最多返回多少条引用
     * @return citations
     */
    private List<ChatAskResponse.Citation> buildCitations(List<Document> docs, int maxDocs) {
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }
        List<ChatAskResponse.Citation> citations = new ArrayList<>();
        for (Document d : docs) {
            if (d == null || d.getMetadata() == null) {
                continue;
            }
            Map<String, Object> md = d.getMetadata();
            String documentId = Objects.toString(md.get("documentId"), null);
            String fileName = Objects.toString(md.get("fileName"), null);
            Integer chunkIndex = null;
            try {
                Object v = md.get("chunkIndex");
                if (v instanceof Number) {
                    chunkIndex = ((Number) v).intValue();
                } else if (v != null) {
                    chunkIndex = Integer.parseInt(v.toString());
                }
            } catch (Exception ignored) {
            }
            String sourcePath = Objects.toString(md.get("sourcePath"), null);

            ChatAskResponse.Citation c = new ChatAskResponse.Citation();
            c.setDocumentId(documentId);
            c.setFileName(fileName);
            c.setChunkIndex(chunkIndex);
            if (sourcePath != null && !sourcePath.isBlank()) {
                c.setPreviewUrl(minioUtils.getFilePreviewUrl(sourcePath));
            }
            citations.add(c);
            if (citations.size() >= maxDocs) {
                break;
            }
        }
        return citations;
    }

}
