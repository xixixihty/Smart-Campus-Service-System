package com.hxq.smart_campus.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiService {

    private final ChatClient chatClient;
    private final ChatClient userChatClient;
    private final ChatClient adminChatClient;

    public AiService(ChatClient chatClient,
                     @Qualifier("userChatClient") ChatClient userChatClient,
                     @Qualifier("adminChatClient") ChatClient adminChatClient) {
        this.chatClient = chatClient;
        this.userChatClient = userChatClient;
        this.adminChatClient = adminChatClient;
    }

    public String analyze(String systemPrompt, String userMessage) {
        log.info("AI分析请求: userMessage={}", userMessage);
        var promptSpec = chatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            promptSpec.system(systemPrompt);
        }
        String result = promptSpec.call()
                .content();
        log.info("AI分析响应: response={}", result);
        return result;
    }

    public Flux<String> analyzeStream(String systemPrompt, String userMessage) {
        log.info("AI流式请求: userMessage={}", userMessage);
        StringBuilder fullResponse = new StringBuilder();
        var promptSpec = chatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            promptSpec.system(systemPrompt);
        }
        Flux<String> stream = promptSpec.stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> log.info("AI流式响应: response={}", fullResponse.toString()));
        return stream.switchIfEmpty(Flux.defer(() -> {
            log.warn("AI流式响应为空，回退到非流式调用");
            String result = analyze(systemPrompt, userMessage);
            if (StringUtils.hasText(result)) {
                return Flux.just(result);
            }
            return Flux.empty();
        }));
    }

    public String analyzeWithUserTools(String systemPrompt, String userMessage) {
        log.info("AI用户端请求: userMessage={}", userMessage);
        var promptSpec = userChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            promptSpec.system(systemPrompt);
        }
        String result = promptSpec.call()
                .content();
        log.info("AI用户端响应: response={}", result);
        return result;
    }

    public Flux<String> analyzeStreamWithUserTools(String systemPrompt, String userMessage) {
        log.info("AI用户端流式请求: userMessage={}", userMessage);
        StringBuilder fullResponse = new StringBuilder();
        var promptSpec = userChatClient.prompt()
                .user(userMessage);
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            promptSpec.system(systemPrompt);
        }
        Flux<String> stream = promptSpec.stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> log.info("AI用户端流式响应: response={}", fullResponse.toString()));
        // 如果流式响应为空，回退到非流式调用（处理工具调用场景）
        return stream.switchIfEmpty(Flux.defer(() -> {
            log.warn("AI用户端流式响应为空，回退到非流式调用");
            String result = analyzeWithUserTools(systemPrompt, userMessage);
            if (StringUtils.hasText(result)) {
                return Flux.just(result);
            }
            return Flux.empty();
        }));
    }

    public String analyzeWithAdminTools(String systemPrompt, String userMessage) {
        log.info("AI管理端请求: userMessage={}", userMessage);
        var promptSpec = adminChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            promptSpec.system(systemPrompt);
        }
        String result = promptSpec.call()
                .content();
        log.info("AI管理端响应: response={}", result);
        return result;
    }

    public Flux<String> analyzeStreamWithAdminTools(String systemPrompt, String userMessage) {
        log.info("AI管理端流式请求: userMessage={}", userMessage);
        StringBuilder fullResponse = new StringBuilder();
        var promptSpec = adminChatClient.prompt()
                .user(userMessage);
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            promptSpec.system(systemPrompt);
        }
        Flux<String> stream = promptSpec.stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> log.info("AI管理端流式响应: response={}", fullResponse.toString()));
        // 如果流式响应为空，回退到非流式调用（处理工具调用场景）
        return stream.switchIfEmpty(Flux.defer(() -> {
            log.warn("AI管理端流式响应为空，回退到非流式调用");
            String result = analyzeWithAdminTools(systemPrompt, userMessage);
            if (StringUtils.hasText(result)) {
                return Flux.just(result);
            }
            return Flux.empty();
        }));
    }
}