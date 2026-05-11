package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.LeaveApprovalDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestCreateDTO;
import com.hxq.smart_campus.entity.vo.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LeaveApprovalMapper {
    /**
     * 提交请假申请
     * @param leaveRequestCreateDTO
     * @return
     */
    int insertLeaveRequest(LeaveRequestCreateDTO leaveRequestCreateDTO);

    /**
     * 获取最后插入的ID
     * @return
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 获取请假申请详情
     * @param id
     * @return
     */
    LeaveRequestDetailVO getLeaveRequestDetail(Long id);

    /**
     * 审批请假申请
     * @param leaveApprovalDTO
     * @return
     */
    int approveLeaveRequest(LeaveApprovalDTO leaveApprovalDTO);


    /**
     * 更新请假申请状态（乐观锁：只在当前状态为'待审批'时更新）
     * @param id
     * @param leaveApplyStatusApproved
     * @return
     */
    @Update("UPDATE leave_request SET status = #{leaveApplyStatusApproved} , update_time = now() WHERE id = #{id} AND status = '待审批'")
    int updateLeaveRequestStatus(@Param("id") Long id,
                                 @Param("leaveApplyStatusApproved") String leaveApplyStatusApproved);

    /**
     * 获取请假申请列表
     * @param studentName
     * @param leaveType
     * @param status
     * @return
     */
    List<LeaveRequestListVO> getLeaveRequestList(@Param("studentName") String studentName,
                                                 @Param("leaveType") String leaveType,
                                                 @Param("status") String status);

    /**
     * 获取我的请假申请列表
     * @param status
     * @param studentId
     * @return
     */
    List<MyLeaveRequestVO> getMyLeaveRequestList(@Param("status") String status,
                                           @Param("studentId") Long studentId);

    /**
     * 获取待审批请假申请列表
     * @param leaveApplyStatusWaiting
     * @return
     */
    List<PendingLeaveRequestVO> getPendingApprovalList(String leaveApplyStatusWaiting);

    /**
     * 获取请假审批审批记录列表
     * @param leaveRequestId
     * @return
     */
    List<LeaveApprovalLogVO> getLeaveApprovalLogList(@Param("leaveRequestId") Long leaveRequestId);
}
