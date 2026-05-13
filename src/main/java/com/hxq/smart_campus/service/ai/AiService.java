package com.hxq.smart_campus.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    public String analyze(String systemPrompt, String userMessage) {
        log.info("AI分析请求: userMessage={}", truncate(userMessage, 100));
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
        log.info("AI分析完成: responseLength={}", result != null ? result.length() : 0);
        return result;
    }

    public Flux<String> analyzeStream(String systemPrompt, String userMessage) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .stream()
                .content();
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }
}
