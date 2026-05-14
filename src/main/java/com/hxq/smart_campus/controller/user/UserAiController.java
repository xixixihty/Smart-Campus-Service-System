package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.dto.AiChatDTO;
import com.hxq.smart_campus.service.ai.AiService;
import com.hxq.smart_campus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai/user")
@Tag(name = "用户端AI学习助手")
@RequiredArgsConstructor
@Slf4j
public class UserAiController {

    private final AiService aiService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI学习助手对话（流式）")
    public Flux<String> chat(@RequestBody AiChatDTO chatDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUserName();
        log.info("用户端AI对话，用户ID：{}，消息：{}", userId, chatDTO.getMessage());
        // 将当前用户上下文注入到system prompt，使AI工具能获取到用户ID参数
        String userContext = String.format(
                "当前用户信息：用户ID=%d, 用户名=%s。当用户查询个人数据时，使用该用户ID作为学生ID参数。",
                userId, userName != null ? userName : "");
        return aiService.analyzeStreamWithUserTools(userContext, chatDTO.getMessage())
                .onErrorResume(e -> {
                    log.error("AI对话异常", e);
                    return Flux.just("抱歉，AI服务暂时不可用，请稍后再试。");
                });
    }
}