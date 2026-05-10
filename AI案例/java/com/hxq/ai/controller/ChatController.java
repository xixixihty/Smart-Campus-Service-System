package com.hxq.ai.controller;


import com.hxq.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {
    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String prompt, @RequestParam(required = false) String chatId) {
        String conversationId = (chatId != null && !chatId.isEmpty()) ? chatId : "session-" + UUID.randomUUID();
        // 保存会话ID
        chatHistoryRepository.save("chat", conversationId);
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()// 流式调用
                .content()
                .onErrorResume(e -> {
                    System.err.println("AI调用失败: " + e.getMessage());
                    return Flux.just("{\"error\": \"AI服务暂时不可用，请稍后重试。错误信息: " + e.getMessage() + "\"}");
                });
    }
}