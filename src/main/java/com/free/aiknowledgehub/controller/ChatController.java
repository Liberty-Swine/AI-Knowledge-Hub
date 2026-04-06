package com.free.aiknowledgehub.controller;

import com.free.aiknowledgehub.common.result.Result;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description AI对话
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:13
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OllamaChatModel ollamaChatModel;

    public ChatController(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
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

}
