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
     * @param applicantId
     * @param applicantType
     * @return
     */
    List<MyLeaveRequestVO> getMyLeaveRequestList(@Param("status") String status,
                                           @Param("applicantId") Long applicantId,
                                           @Param("applicantType") String applicantType);

    /**
     * 获取待审批请假申请列表
     * @param leaveApplyStatusWaiting
     * @param approverId
     * @return
     */
    List<PendingLeaveRequestVO> getPendingApprovalList(@Param("status") String leaveApplyStatusWaiting,
                                                        @Param("approverId") Long approverId);

    /**
     * 获取请假审批审批记录列表
     * @param leaveRequestId
     * @return
     */
    List<LeaveApprovalLogVO> getLeaveApprovalLogList(@Param("leaveRequestId") Long leaveRequestId);

    /**
     * 查询学生对应的班主任ID
     * @param studentId
     * @return
     */
    Long getHeadTeacherByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询教师对应的学院管理员ID
     * @param teacherId
     * @return
     */
    Long getCollegeAdminByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 获取所有启用的管理员（作为可选审批人）
     * @return
     */
    @Select("SELECT a.id, a.name, 'ADMIN' as type FROM admin a WHERE a.account_status = '启用' ORDER BY a.id")
    List<ApproverVO> getAllEnabledAdmins();

    /**
     * 根据教师ID获取教师信息（作为审批人选项）
     * @param teacherId
     * @return
     */
    @Select("SELECT t.id, t.name, 'TEACHER' as type FROM teacher t WHERE t.id = #{teacherId}")
    ApproverVO getTeacherApproverById(@Param("teacherId") Long teacherId);
}
