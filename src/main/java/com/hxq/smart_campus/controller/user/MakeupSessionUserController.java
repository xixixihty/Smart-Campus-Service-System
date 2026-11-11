package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.vo.MakeupExamSessionDetailVO;
import com.hxq.smart_campus.entity.vo.MyMakeupSessionVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.MakeupExamSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 补考场次查询（学生端）
 */
@RestController
@RequestMapping("/api/makeup-sessions/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "我的补考（新）")
public class MakeupSessionUserController {

    private final MakeupExamSessionService sessionService;

    /**
     * 查询我的补考列表
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的补考列表")
    public Result<List<MyMakeupSessionVO>> getMySessions(
            @RequestParam(required = false) String status) {
        log.info("查询我的补考列表: status={}", status);
        List<MyMakeupSessionVO> result = sessionService.getMySessions(status);
        return Result.success(result);
    }

    /**
     * 查询补考场次详情（验证学生是否属于该场次）
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询补考场次详情")
    public Result<MakeupExamSessionDetailVO> getSessionDetail(@PathVariable Long id) {
        log.info("查询补考场次详情: {}", id);
        MakeupExamSessionDetailVO result = sessionService.getSessionDetail(id);
        return Result.success(result);
    }
}