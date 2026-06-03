package com.hxq.smart_campus.controller.admin;

import com.hxq.smart_campus.entity.vo.CourseRescheduleVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseRescheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@Tag(name = "管理员调课审批模块")
@RequiredArgsConstructor
@Validated
public class CourseRescheduleApprovalController {

    private final CourseRescheduleService courseRescheduleService;

    @GetMapping("/reschedules/pending")
    @Operation(summary = "获取待审批调课列表")
    public Result<List<CourseRescheduleVO>> getPendingReschedules() {
        log.info("管理员查询待审批调课列表");
        List<CourseRescheduleVO> list = courseRescheduleService.getPendingList();
        return Result.success(list);
    }

    @GetMapping("/reschedule/{id}")
    @Operation(summary = "查看调课详情")
    public Result<CourseRescheduleVO> getRescheduleDetail(@PathVariable Long id) {
        log.info("管理员查看调课详情: id={}", id);
        CourseRescheduleVO detail = courseRescheduleService.getRescheduleDetail(id);
        return Result.success(detail);
    }

    @PostMapping("/reschedule/{id}/approve")
    @Operation(summary = "审批通过调课申请")
    public Result<?> approveReschedule(@PathVariable Long id) {
        log.info("管理员审批通过调课: id={}", id);
        courseRescheduleService.approveReschedule(id);
        return Result.success();
    }

    @PostMapping("/reschedule/{id}/reject")
    @Operation(summary = "驳回调课申请")
    public Result<?> rejectReschedule(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        log.info("管理员驳回调课: id={}, reason={}", id, reason);
        courseRescheduleService.rejectReschedule(id, reason);
        return Result.success();
    }
}