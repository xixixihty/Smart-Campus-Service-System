package com.hxq.smart_campus.controller.admin;

import com.hxq.smart_campus.entity.dto.AiChatDTO;
import com.hxq.smart_campus.service.ai.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai/admin")
@Tag(name = "管理端AI智慧助手")
@RequiredArgsConstructor
@Slf4j
public class AdminAiController {

    private final AiService aiService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI智慧助手对话（流式）")
    public Flux<ServerSentEvent<String>> chat(@RequestBody AiChatDTO chatDTO) {
        log.info("管理端AI对话: sessionId={}, message={}", chatDTO.getSessionId(), chatDTO.getMessage());
        return aiService.chatStreamWithAdminTools("", chatDTO.getMessage(), chatDTO.getSessionId())
                .onErrorResume(e -> {
                    log.error("管理端AI对话异常", e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("抱歉，AI服务暂时不可用，请稍后再试。")
                            .build());
                });
    }
}