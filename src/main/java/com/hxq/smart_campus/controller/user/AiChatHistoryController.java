package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.entity.dto.AiChatHistoryDTO;
import com.hxq.smart_campus.entity.dto.AiSessionDTO;
import com.hxq.smart_campus.service.AiChatRecordService;
import com.hxq.smart_campus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/chat")
@Slf4j
@RequiredArgsConstructor
public class AiChatHistoryController {

    private final AiChatRecordService aiChatRecordService;

    /**
     * 获取当前用户的AI对话会话列表
     * @return
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取AI对话会话列表")
    public Result<List<AiSessionDTO>> listSessions() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("查询AI对话会话列表: userId={}", userId);
        return Result.success(aiChatRecordService.listSessions(userId));
    }

    /**
     * 获取指定会话的对话记录
     * @param sessionId
     * @param limit
     * @return
     */
    @GetMapping("/history/{sessionId}")
    @Operation(summary = "获取AI对话历史记录")
    public Result<List<AiChatHistoryDTO>> getHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "100") int limit
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("查询AI对话记录: sessionId={}, userId={}, limit={}", sessionId, userId, limit);
        return Result.success(aiChatRecordService.getSessionHistory(sessionId, userId, limit));
    }

    /**
     * 删除对话会话
     * @param sessionId
     * @return
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除AI对话会话")
    public Result<Boolean> deleteSession(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("删除AI对话会话: sessionId={}, userId={}", sessionId, userId);
        return Result.success(aiChatRecordService.deleteSession(sessionId, userId));
    }
}