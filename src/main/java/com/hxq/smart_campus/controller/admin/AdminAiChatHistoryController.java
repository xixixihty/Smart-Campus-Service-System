package com.hxq.smart_campus.controller.admin;

import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.entity.dto.AiChatHistoryDTO;
import com.hxq.smart_campus.entity.dto.AiSessionDTO;
import com.hxq.smart_campus.service.AiChatRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/admin/chat")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "管理端AI对话历史")
public class AdminAiChatHistoryController {

    private static final Long ADMIN_USER_ID = 0L;
    private final AiChatRecordService aiChatRecordService;

    @GetMapping("/sessions")
    @Operation(summary = "获取管理端AI对话会话列表")
    public Result<List<AiSessionDTO>> listSessions() {
        log.info("管理端查询AI对话会话列表");
        return Result.success(aiChatRecordService.listSessions(ADMIN_USER_ID));
    }

    @GetMapping("/history/{sessionId}")
    @Operation(summary = "获取管理端AI对话历史记录")
    public Result<List<AiChatHistoryDTO>> getHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("管理端查询AI对话记录: sessionId={}, limit={}", sessionId, limit);
        return Result.success(aiChatRecordService.getSessionHistory(sessionId, limit));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除管理端AI对话会话")
    public Result<Boolean> deleteSession(@PathVariable String sessionId) {
        log.info("管理端删除AI对话会话: sessionId={}", sessionId);
        return Result.success(aiChatRecordService.deleteSession(sessionId, ADMIN_USER_ID));
    }
}