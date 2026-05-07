package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.dto.LeaveRequestCreateDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestResponseDTO;
import com.hxq.smart_campus.entity.vo.LeaveRequestDetailVO;
import com.hxq.smart_campus.entity.vo.MyLeaveRequestVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.LeaveApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests/user")
@RequiredArgsConstructor
@Tag(name = "请假管理模块-用户端")
@Slf4j
public class LeaveApprovalUserController {
    private final LeaveApprovalService leaveApprovalService;

    /**
     * 提交请假申请
     * @param leaveRequestCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "提交请假申请")
    public Result<LeaveRequestResponseDTO> insertLeaveRequest(@RequestBody LeaveRequestCreateDTO leaveRequestCreateDTO) {
        log.info("提交请假申请：{}", leaveRequestCreateDTO);
        LeaveRequestResponseDTO leaveRequestResponseDTO = leaveApprovalService.insertLeaveRequest(leaveRequestCreateDTO);
        return Result.success(leaveRequestResponseDTO);
    }


    /**
     * 取消请假申请
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "取消请假申请")
    public Result<Boolean> cancelLeaveRequest(@PathVariable Long id) {
        log.info("取消请假申请：{}", id);
        Boolean result = leaveApprovalService.cancelLeaveRequest(id);
        return Result.success(result);
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

    /**
     * 查询我的请假列表
     * @param status
     * @return
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的请假列表")
    public Result<List<MyLeaveRequestVO>> getMyLeaveRequestList(@RequestParam(required = false) String status) {
        log.info("查询我的请假列表：{}", status);
        List<MyLeaveRequestVO> myLeaveRequestList = leaveApprovalService.getMyLeaveRequestList(status);
        return Result.success(myLeaveRequestList);
    }
}
