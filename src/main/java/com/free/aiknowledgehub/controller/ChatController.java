package com.free.aiknowledgehub.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.free.aiknowledgehub.common.result.Result;
import com.free.aiknowledgehub.hook.RAGMessagesHook;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description AI对话
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:13
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OllamaChatModel ollamaChatModel;

    private final VectorStore vectorStore;


    public ChatController(OllamaChatModel ollamaChatModel, VectorStore vectorStore) {
        this.ollamaChatModel = ollamaChatModel;
        this.vectorStore = vectorStore;
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


//    /**
//     * rag对话 todo 因为依赖冲突，导致使用不了
//     * @param question
//     * @return
//     */
//    @GetMapping("/ragChat/{question}")
//    public Result ragChat(@PathVariable(value="question") String question){
//        // 创建带有 RAG Hook 的 Agent
//        ReactAgent ragAgent = ReactAgent.builder()
//                .name("rag_agent")
//                .model(ollamaChatModel)
//                .hooks(new RAGMessagesHook(vectorStore))
//                .build();
//        // 调用 Agent
//        try {
//            AssistantMessage response = ragAgent.call(question);
//            return Result.success(response);
//        } catch (GraphRunnerException e) {
//            e.printStackTrace();
//        }
//        return Result.error();
//    }

    /**
     * rag查询测试
     * @param question
     * @return
     */
    @GetMapping("/ragChat/{question}")
    public Result ragChat(@PathVariable(value="question") String question){
        // 1. 【检索】手动去向量数据库搜索相似文档
        SearchRequest searchRequest = SearchRequest.builder().query(question)
                .topK(3)           // 【关键】限制只返回最相似的 3 条（默认是 4）
                .similarityThreshold(0.75)
                .build(); // 【核心】设置相似度阈值 (0.0 - 1.0)
        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
        // 2. 【处理】如果没有找到文档，直接返回或进行普通聊天
        if (similarDocuments.isEmpty()) {
            return Result.error("未找到相关知识库内容。") ;
        }
        // 3. 【组装】提取文档内容，拼接成上下文
        // 将多个文档的内容用换行符拼起来
        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
        // 4. 【生成】构建提示词，把上下文和问题一起发给大模型
        String userPrompt = """
            你是一个智能助手。请根据以下参考信息回答问题。
            如果参考信息中没有答案，请直接说你不知道。
            
            参考信息：
            %s
            
            问题：
            %s
            """.formatted(context, question);

        ChatClient chatClient=ChatClient.builder(ollamaChatModel).build();

        // 5. 【调用】使用原生的 ChatClient 发送请求并获取结果
        return Result.success(chatClient.prompt()
                .user(userPrompt)
                .call()
                .content()) ;
    }

}
