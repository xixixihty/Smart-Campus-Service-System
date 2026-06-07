package com.hxq.smart_campus.controller.teacher;

import com.hxq.smart_campus.entity.dto.AiChatDTO;
import com.hxq.smart_campus.service.ai.AiService;
import com.hxq.smart_campus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 教师端 AI 助手控制器
 */
@RestController
@RequestMapping("/api/ai/teacher")
@Tag(name = "教师端AI助手")
@RequiredArgsConstructor
@Slf4j
public class TeacherAiController {

    private final AiService aiService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI助手对话（流式）")
    public Flux<ServerSentEvent<String>> chat(@RequestBody AiChatDTO chatDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUserName();
        log.info("教师端AI对话: userId={}, sessionId={}, message={}", userId, chatDTO.getSessionId(), chatDTO.getMessage());

        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append(String.format(
                "当前教师信息：教师ID=%d, 姓名=%s。当查询教学数据时，使用该教师ID。",
                userId, userName != null ? userName : ""));

        if (chatDTO.getContext() != null && !chatDTO.getContext().isEmpty()) {
            systemPrompt.append(" 附加上下文：").append(chatDTO.getContext());
        }

        return aiService.chatStreamWithTeacherTools(
                systemPrompt.toString(),
                chatDTO.getMessage(),
                chatDTO.getSessionId(),
                userId)
                .onErrorResume(e -> {
                    log.error("教师端AI对话异常", e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("抱歉，AI服务暂时不可用，请稍后再试。")
                            .build());
                });
    }
}