package com.hxq.smart_campus.controller.admin;


import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.LeaveApprovalDTO;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.LeaveApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests/admin")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "请假管理模块")
public class LeaveApprovalController {
    private final LeaveApprovalService leaveApprovalService;


//    /**
//     * 提交请假申请
//     * @param leaveRequestCreateDTO
//     * @return
//     */
//    @PostMapping
//    @Operation(summary = "提交请假申请")
//    public Result<LeaveRequestResponseDTO> insertLeaveRequest(@RequestBody LeaveRequestCreateDTO leaveRequestCreateDTO) {
//        log.info("提交请假申请：{}", leaveRequestCreateDTO);
//        LeaveRequestResponseDTO leaveRequestResponseDTO = leaveApprovalService.insertLeaveRequest(leaveRequestCreateDTO);
//        return Result.success(leaveRequestResponseDTO);
//    }

    /**
     * 审批请假申请
     * @param leaveApprovalDTO
     * @return
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批请假申请")
    public Result<Boolean> approveLeaveRequest(@PathVariable Long id,
                                               @RequestBody LeaveApprovalDTO leaveApprovalDTO) {
        log.info("审批请假申请：{}", id, leaveApprovalDTO);
        Boolean result = leaveApprovalService.approveLeaveRequest(id, leaveApprovalDTO);
        return Result.success(result);
    }
//    /**
//     * 取消请假申请
//     */
//    @DeleteMapping("/{id}")
//    @Operation(summary = "取消请假申请")
//    public Result<Boolean> cancelLeaveRequest(@PathVariable Long id) {
//        log.info("取消请假申请：{}", id);
//        Boolean result = leaveApprovalService.cancelLeaveRequest(id);
//        return Result.success(result);
//    }
    /**
     * 查询请假列表
     */
    @GetMapping
    @Operation(summary = "查询请假列表")
    public Result<PageInfo<LeaveRequestListVO>> getLeaveRequestList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String status
    ) {
        log.info("查询请假列表：pageNum={}, pageSize={}, studentName={}, leaveType={}, status={}", pageNum, pageSize, studentName, leaveType, status);
        PageInfo<LeaveRequestListVO> leaveRequestList = leaveApprovalService.getLeaveRequestList(pageNum, pageSize, studentName, leaveType, status);
        return Result.success(leaveRequestList);
    }

    /**
     * 查询请假详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询请假详情")
    public Result<LeaveRequestDetailVO> getLeaveRequestDetail(@PathVariable Long id) {
        log.info("查询请假详情：{}", id);
        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalService.getLeaveRequestDetail(id);
        return Result.success(leaveRequestDetailVO);
    }

//    /**
//     * 查询我的请假列表
//     * @param status
//     * @return
//     */
//    @GetMapping("/my")
//    @Operation(summary = "查询我的请假列表")
//    public Result<List<MyLeaveRequestVO>> getMyLeaveRequestList(@RequestParam(required = false) String status) {
//        log.info("查询我的请假列表：{}", status);
//        List<MyLeaveRequestVO> myLeaveRequestList = leaveApprovalService.getMyLeaveRequestList(status);
//        return Result.success(myLeaveRequestList);
//    }

    /**
     * 查询待审批列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/pending")
    @Operation(summary = "查询待审批列表")
    public Result<PageInfo<PendingLeaveRequestVO>> getPendingApprovalList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("查询待审批列表：{}", pageNum, pageSize);
        PageInfo<PendingLeaveRequestVO> pendingApprovalList = leaveApprovalService.getPendingApprovalList(pageNum, pageSize);
        return Result.success(pendingApprovalList);
    }

    /**
     * 查询审批记录
     * @param leaveRequestId
     * @return
     */
    @GetMapping("/approval-log")
    @Operation(summary = "查询审批记录")
    public Result<List<LeaveApprovalLogVO>> getLeaveApprovalLogList(@RequestParam(required = false) Long leaveRequestId) {
        log.info("查询审批记录：{}", leaveRequestId);
        List<LeaveApprovalLogVO> leaveApprovalLogList = leaveApprovalService.getLeaveApprovalLogList(leaveRequestId);
        return Result.success(leaveApprovalLogList);
    }
}
