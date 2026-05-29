package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.service.AiChatRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AiService {

    private static final Long ADMIN_USER_ID = 0L;

    private final ChatClient userChatClient;
    private final ChatClient adminChatClient;
    private final ChatMemory chatMemory;
    private final AiChatRecordService aiChatRecordService;

    public AiService(@Qualifier("userChatClient") ChatClient userChatClient,
                     @Qualifier("adminChatClient") ChatClient adminChatClient,
                     ChatMemory chatMemory,
                     AiChatRecordService aiChatRecordService) {
        this.userChatClient = userChatClient;
        this.adminChatClient = adminChatClient;
        this.chatMemory = chatMemory;
        this.aiChatRecordService = aiChatRecordService;
    }

    public Flux<ServerSentEvent<String>> chatStreamWithUserTools(String systemPrompt, String userMessage,
                                                                  String sessionId, Long userId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        String effectiveSessionId = sessionId;
        log.info("AI用户端流式请求: sessionId={}, userId={}, userMessage={}", effectiveSessionId, userId, userMessage);

        StringBuilder fullResponse = new StringBuilder();

        ChatClient.ChatClientRequestSpec spec = userChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }

        Flux<String> contentStream = spec
                .toolContext(Map.of("studentId", userId))
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(effectiveSessionId).build())
                .stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    log.info("AI用户端流式响应完成: sessionId={}, response={}",
                            effectiveSessionId, fullResponse.toString());
                    aiChatRecordService.saveMessagesAsync(userId, effectiveSessionId,
                            userMessage, fullResponse.toString());
                })
                .onErrorResume(e -> {
                    log.warn("AI流式调用失败，自动降级为非流式模式: sessionId={}, userId={}, error={}",
                            effectiveSessionId, userId, e.getMessage());
                    try {
                        String result = chatWithUserTools(systemPrompt, userMessage, effectiveSessionId, userId);
                        if (StringUtils.hasText(result)) {
                            fullResponse.append(result);
                            return Flux.just(result);
                        }
                    } catch (Exception ex) {
                        log.error("AI非流式降级也失败了: sessionId={}, userId={}", effectiveSessionId, userId, ex);
                    }
                    return Flux.error(new RuntimeException("AI服务调用失败，流式和非流式均不可用", e));
                });

        return wrapStreamWithEvents(contentStream, effectiveSessionId, () ->
                chatWithUserTools(systemPrompt, userMessage, effectiveSessionId, userId));
    }

    public Flux<ServerSentEvent<String>> chatStreamWithAdminTools(String systemPrompt, String userMessage,
                                                                   String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        String effectiveSessionId = sessionId;
        log.info("AI管理端流式请求: sessionId={}, userMessage={}", effectiveSessionId, userMessage);

        StringBuilder fullResponse = new StringBuilder();

        ChatClient.ChatClientRequestSpec spec = adminChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }

        Flux<String> contentStream = spec
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(effectiveSessionId).build())
                .stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    String response = fullResponse.toString();
                    log.info("AI管理端流式响应完成: sessionId={}, response={}",
                            effectiveSessionId, response);
                    aiChatRecordService.saveMessagesAsync(ADMIN_USER_ID, effectiveSessionId,
                            userMessage, response);
                })
                .onErrorResume(e -> {
                    log.warn("AI流式调用失败，自动降级为非流式模式: sessionId={}, error={}",
                            effectiveSessionId, e.getMessage());
                    try {
                        String result = chatWithAdminTools(systemPrompt, userMessage, effectiveSessionId);
                        if (StringUtils.hasText(result)) {
                            fullResponse.append(result);
                            return Flux.just(result);
                        }
                    } catch (Exception ex) {
                        log.error("AI非流式降级也失败了: sessionId={}", effectiveSessionId, ex);
                    }
                    return Flux.error(new RuntimeException("AI服务调用失败，流式和非流式均不可用", e));
                });

        return wrapStreamWithEvents(contentStream, effectiveSessionId, () ->
                chatWithAdminTools(systemPrompt, userMessage, effectiveSessionId));
    }

    private String chatWithUserTools(String systemPrompt, String userMessage, String sessionId, Long userId) {
        log.info("AI用户端非流式回退: sessionId={}, userId={}", sessionId, userId);
        ChatClient.ChatClientRequestSpec spec = userChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }
        String result = spec
                .toolContext(Map.of("studentId", userId))
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(sessionId).build())
                .call()
                .content();
        log.info("AI用户端非流式响应: sessionId={}, response={}", sessionId, result);
        return result;
    }

    private String chatWithAdminTools(String systemPrompt, String userMessage, String sessionId) {
        log.info("AI管理端非流式回退: sessionId={}", sessionId);
        ChatClient.ChatClientRequestSpec spec = adminChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }
        String result = spec
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(sessionId).build())
                .call()
                .content();
        log.info("AI管理端非流式响应: sessionId={}, response={}", sessionId, result);
        return result;
    }

    public String analyzeWithAdminTools(String systemPrompt, String userMessage) {
        return chatWithAdminTools(systemPrompt, userMessage, UUID.randomUUID().toString());
    }

    private Flux<ServerSentEvent<String>> wrapStreamWithEvents(Flux<String> contentStream,
                                                                String sessionId,
                                                                FallbackProvider fallback) {
        Flux<ServerSentEvent<String>> messageEvents = contentStream
                .map(content -> ServerSentEvent.<String>builder()
                        .event("message")
                        .data(content)
                        .build());

        Flux<ServerSentEvent<String>> heartbeatFlux = Flux.interval(Duration.ofSeconds(15))
                .map(tick -> ServerSentEvent.<String>builder()
                        .event("heartbeat")
                        .data("")
                        .build())
                .takeUntilOther(messageEvents.then(Mono.just(true)));

        Flux<ServerSentEvent<String>> stream = Flux.concat(
                Flux.just(ServerSentEvent.<String>builder()
                        .event("status")
                        .data("正在分析您的问题...")
                        .build()),
                Flux.merge(messageEvents, heartbeatFlux),
                Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("[DONE]")
                        .build())
        );

        return stream.switchIfEmpty(Flux.defer(() -> {
            log.warn("AI流式响应为空，回退到非流式调用: sessionId={}", sessionId);
            try {
                String result = fallback.execute();
                if (StringUtils.hasText(result)) {
                    return Flux.just(
                            ServerSentEvent.<String>builder()
                                    .event("status")
                                    .data("正在查询数据...")
                                    .build(),
                            ServerSentEvent.<String>builder()
                                    .event("message")
                                    .data(result)
                                    .build(),
                            ServerSentEvent.<String>builder()
                                    .event("done")
                                    .data("[DONE]")
                                    .build()
                    );
                }
            } catch (Exception e) {
                log.error("非流式回退失败: sessionId={}", sessionId, e);
            }
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("抱歉，AI服务暂时不可用，请稍后再试。")
                    .build());
        }));
    }

    @FunctionalInterface
    private interface FallbackProvider {
        String execute();
    }
}