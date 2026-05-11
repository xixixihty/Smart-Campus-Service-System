package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.LeaveApprovalDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestCreateDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestResponseDTO;
import com.hxq.smart_campus.entity.vo.*;

import java.util.List;

public interface LeaveApprovalService {
    /**
     * 提交请假申请
     * @param leaveRequestCreateDTO
     * @return
     */
    LeaveRequestResponseDTO insertLeaveRequest(LeaveRequestCreateDTO leaveRequestCreateDTO);
    /**
     * 审批请假申请
     * @param leaveApprovalDTO
     * @return
     */
    Boolean approveLeaveRequest(Long id, LeaveApprovalDTO leaveApprovalDTO);
    /**
     * 取消请假申请
     * @param id
     * @return
     */
    Boolean cancelLeaveRequest(Long id);

    /**
     * 获取请假申请列表
     * @param pageNum
     * @param pageSize
     * @param studentName
     * @param leaveType
     * @param status
     * @return
     */
    PageInfo<LeaveRequestListVO> getLeaveRequestList(Integer pageNum, Integer pageSize, String studentName, String leaveType, String status);

    /**
     * 获取请假申请详情
     * @param id
     * @return
     */
    LeaveRequestDetailVO getLeaveRequestDetail(Long id);

    /**
     * 获取我的请假申请列表
     * @param status
     * @return
     */
    List<MyLeaveRequestVO> getMyLeaveRequestList(String status);

    /**
     * 获取待审批请假申请列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PendingLeaveRequestVO> getPendingApprovalList(Integer pageNum, Integer pageSize);

    /**
     * 获取请假申请审批日志列表
     * @param leaveRequestId
     * @return
     */
    List<LeaveApprovalLogVO> getLeaveApprovalLogList(Long leaveRequestId);
}
