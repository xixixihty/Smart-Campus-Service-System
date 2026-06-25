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
    private final ChatClient teacherChatClient;
    private final ChatMemory userChatMemory;
    private final ChatMemory adminChatMemory;
    private final ChatMemory teacherChatMemory;
    private final AiChatRecordService aiChatRecordService;

    public AiService(@Qualifier("userChatClient") ChatClient userChatClient,
                     @Qualifier("adminChatClient") ChatClient adminChatClient,
                     @Qualifier("teacherChatClient") ChatClient teacherChatClient,
                     @Qualifier("userChatMemory") ChatMemory userChatMemory,
                     @Qualifier("adminChatMemory") ChatMemory adminChatMemory,
                     @Qualifier("teacherChatMemory") ChatMemory teacherChatMemory,
                     AiChatRecordService aiChatRecordService) {
        this.userChatClient = userChatClient;
        this.adminChatClient = adminChatClient;
        this.teacherChatClient = teacherChatClient;
        this.userChatMemory = userChatMemory;
        this.adminChatMemory = adminChatMemory;
        this.teacherChatMemory = teacherChatMemory;
        this.aiChatRecordService = aiChatRecordService;
    }

    // ===== 用户端流式 =====

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
                .advisors(MessageChatMemoryAdvisor.builder(userChatMemory).conversationId(effectiveSessionId).build())
                .stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    log.info("AI用户端流式响应完成: sessionId={}", effectiveSessionId);
                    aiChatRecordService.saveMessagesAsync(userId, effectiveSessionId,
                            userMessage, fullResponse.toString());
                })
                .onErrorResume(e -> {
                    log.warn("AI用户端流式调用失败, 将回退到非流式: {}", e.getMessage());
                    return Flux.empty();
                });

        return wrapStreamWithEvents(contentStream, effectiveSessionId, () ->
                chatWithUserTools(systemPrompt, userMessage, effectiveSessionId, userId));
    }

    // ===== 管理端流式 =====

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
                .advisors(MessageChatMemoryAdvisor.builder(adminChatMemory).conversationId(effectiveSessionId).build())
                .stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    String response = fullResponse.toString();
                    log.info("AI管理端流式响应完成: sessionId={}", effectiveSessionId);
                    aiChatRecordService.saveMessagesAsync(ADMIN_USER_ID, effectiveSessionId,
                            userMessage, response);
                })
                .onErrorResume(e -> {
                    log.warn("AI管理端流式调用失败, 将回退到非流式: {}", e.getMessage());
                    return Flux.empty();
                });

        return wrapStreamWithEvents(contentStream, effectiveSessionId, () ->
                chatWithAdminTools(systemPrompt, userMessage, effectiveSessionId));
    }

    // ===== 教师端流式 =====

    public Flux<ServerSentEvent<String>> chatStreamWithTeacherTools(String systemPrompt, String userMessage,
                                                                     String sessionId, Long userId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        String effectiveSessionId = sessionId;
        log.info("AI教师端流式请求: sessionId={}, userId={}, userMessage={}", effectiveSessionId, userId, userMessage);

        StringBuilder fullResponse = new StringBuilder();

        ChatClient.ChatClientRequestSpec spec = teacherChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }

        Flux<String> contentStream = spec
                .toolContext(Map.of("teacherId", userId))
                .advisors(MessageChatMemoryAdvisor.builder(teacherChatMemory).conversationId(effectiveSessionId).build())
                .stream()
                .content()
                .filter(msg -> msg != null && !msg.isEmpty())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    log.info("AI教师端流式响应完成: sessionId={}", effectiveSessionId);
                    aiChatRecordService.saveMessagesAsync(userId, effectiveSessionId,
                            userMessage, fullResponse.toString());
                })
                .onErrorResume(e -> {
                    log.warn("AI教师端流式调用失败, 将回退到非流式: {}", e.getMessage());
                    return Flux.empty();
                });

        return wrapStreamWithEvents(contentStream, effectiveSessionId, () ->
                chatWithTeacherTools(systemPrompt, userMessage, effectiveSessionId, userId));
    }

    // ===== 非流式（用户端） =====

    public String chatWithUserTools(String systemPrompt, String userMessage, String sessionId, Long userId) {
        log.info("AI用户端非流式请求: sessionId={}, userId={}", sessionId, userId);
        ChatClient.ChatClientRequestSpec spec = userChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }
        String result = spec
                .toolContext(Map.of("studentId", userId))
                .advisors(MessageChatMemoryAdvisor.builder(userChatMemory).conversationId(sessionId).build())
                .call()
                .content();
        log.info("AI用户端非流式响应: sessionId={}", sessionId);
        return result;
    }

    // ===== 非流式（管理端） =====

    public String analyzeWithAdminTools(String systemPrompt, String userMessage) {
        String sessionId = UUID.randomUUID().toString();
        log.info("AI管理端非流式请求: sessionId={}", sessionId);
        return adminChatClient.prompt()
                .user(userMessage)
                .advisors(MessageChatMemoryAdvisor.builder(adminChatMemory).conversationId(sessionId).build())
                .call()
                .content();
    }

    // ===== 非流式回退（管理端） =====

    private String chatWithAdminTools(String systemPrompt, String userMessage, String sessionId) {
        log.info("AI管理端非流式回退: sessionId={}", sessionId);
        ChatClient.ChatClientRequestSpec spec = adminChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }
        String result = spec
                .advisors(MessageChatMemoryAdvisor.builder(adminChatMemory).conversationId(sessionId).build())
                .call()
                .content();
        log.info("AI管理端非流式回退完成: sessionId={}", sessionId);
        return result;
    }

    // ===== 非流式回退（教师端） =====

    private String chatWithTeacherTools(String systemPrompt, String userMessage, String sessionId, Long userId) {
        log.info("AI教师端非流式回退: sessionId={}, userId={}", sessionId, userId);
        ChatClient.ChatClientRequestSpec spec = teacherChatClient.prompt()
                .user(userMessage);
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }
        String result = spec
                .toolContext(Map.of("teacherId", userId))
                .advisors(MessageChatMemoryAdvisor.builder(teacherChatMemory).conversationId(sessionId).build())
                .call()
                .content();
        log.info("AI教师端非流式回退完成: sessionId={}", sessionId);
        return result;
    }

    // ===== 流式事件包装 =====

    private Flux<ServerSentEvent<String>> wrapStreamWithEvents(Flux<String> contentStream,
                                                                String sessionId,
                                                                java.util.function.Supplier<String> fallback) {
        return Flux.concat(
                Flux.just(ServerSentEvent.builder("connected").event("status").build()),
                contentStream.map(data -> ServerSentEvent.builder(data).event("message").build()),
                Flux.just(ServerSentEvent.builder("[DONE]").event("message").build())
        )
        .timeout(Duration.ofSeconds(60))
        .onErrorResume(e -> {
            log.warn("AI流式调用超时或出错, 回退到非流式: {}", e.getMessage());
            try {
                String fallbackResult = fallback.get();
                if (fallbackResult != null && !fallbackResult.isEmpty()) {
                    return Flux.just(
                            ServerSentEvent.builder(fallbackResult).event("message").build(),
                            ServerSentEvent.builder("[DONE]").event("message").build()
                    );
                }
            } catch (Exception ex) {
                log.error("非流式回退也失败", ex);
            }
            return Flux.just(ServerSentEvent.builder("抱歉，AI服务暂时不可用，请稍后再试。").event("message").build(),
                    ServerSentEvent.builder("[DONE]").event("message").build());
        });
    }
}