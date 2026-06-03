package com.hxq.smart_campus.controller.teacher;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.LeaveApprovalDTO;
import com.hxq.smart_campus.entity.vo.LeaveApprovalLogVO;
import com.hxq.smart_campus.entity.vo.LeaveRequestDetailVO;
import com.hxq.smart_campus.entity.vo.PendingLeaveRequestVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.LeaveApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@Slf4j
@Tag(name = "教师请假审批模块")
@RequiredArgsConstructor
@Validated
public class TeacherLeaveApprovalController {

    private final LeaveApprovalService leaveApprovalService;

    @GetMapping("/leave-requests/pending")
    @Operation(summary = "获取待审批请假列表")
    public Result<PageInfo<PendingLeaveRequestVO>> getPendingApprovals(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("教师查询待审批请假: pageNum={}, pageSize={}", pageNum, pageSize);
        PageInfo<PendingLeaveRequestVO> list = leaveApprovalService.getPendingApprovalList(pageNum, pageSize);
        return Result.success(list);
    }

    @GetMapping("/leave-request/{id}")
    @Operation(summary = "查看请假申请详情")
    public Result<LeaveRequestDetailVO> getLeaveDetail(@PathVariable Long id) {
        log.info("教师查看请假详情: id={}", id);
        LeaveRequestDetailVO detail = leaveApprovalService.getLeaveRequestDetail(id);
        return Result.success(detail);
    }

    @PostMapping("/leave-request/{id}/approve")
    @Operation(summary = "审批请假申请")
    public Result<?> approveLeaveRequest(@PathVariable Long id, @Valid @RequestBody LeaveApprovalDTO dto) {
        log.info("教师审批请假: id={}, action={}", id, dto.getAction());
        leaveApprovalService.approveLeaveRequest(id, dto);
        return Result.success();
    }

    @GetMapping("/leave-request/{id}/logs")
    @Operation(summary = "查看请假审批日志")
    public Result<List<LeaveApprovalLogVO>> getApprovalLogs(@PathVariable Long id) {
        log.info("教师查看请假审批日志: id={}", id);
        List<LeaveApprovalLogVO> logs = leaveApprovalService.getLeaveApprovalLogList(id);
        return Result.success(logs);
    }
}