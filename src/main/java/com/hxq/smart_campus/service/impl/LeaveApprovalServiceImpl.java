package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.LeaveApprovalDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestCreateDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestResponseDTO;
import com.hxq.smart_campus.entity.pojo.Notification;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.exception.BusinessException;
import com.hxq.smart_campus.exception.NotFoundException;
import com.hxq.smart_campus.mapper.LeaveApprovalMapper;
import com.hxq.smart_campus.service.LeaveApprovalService;
import com.hxq.smart_campus.service.LeaveNotificationService;
import com.hxq.smart_campus.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveApprovalServiceImpl implements LeaveApprovalService {
    private final LeaveApprovalMapper leaveApprovalMapper;
    private final LeaveNotificationService leaveNotificationService;

    @Override
    public LeaveRequestResponseDTO insertLeaveRequest(LeaveRequestCreateDTO leaveRequestCreateDTO) {
        if (leaveRequestCreateDTO == null) {
            throw new IllegalArgumentException("请假申请参数不能为空");
        }
        if (leaveRequestCreateDTO.getLeaveType() == null || leaveRequestCreateDTO.getLeaveType().isEmpty()) {
            throw new IllegalArgumentException("请假类型不能为空");
        }
        if (leaveRequestCreateDTO.getStartTime() == null) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (leaveRequestCreateDTO.getEndTime() == null) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        if (leaveRequestCreateDTO.getEndTime().isBefore(leaveRequestCreateDTO.getStartTime())) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }
        if (leaveRequestCreateDTO.getReason() == null || leaveRequestCreateDTO.getReason().isEmpty()) {
            throw new IllegalArgumentException("请假事由不能为空");
        }

        String userType = SecurityUtils.getCurrentUserType();
        Long userId = SecurityUtils.getCurrentUserId();

        if (USER_TYPE_STUDENT.equals(userType)) {
            leaveRequestCreateDTO.setApplicantType(APPLICANT_TYPE_STUDENT);
            leaveRequestCreateDTO.setApplicantId(userId);
            leaveRequestCreateDTO.setStudentId(userId);
            if (leaveRequestCreateDTO.getApproverId() == null) {
                Long approverId = leaveApprovalMapper.getHeadTeacherByStudentId(userId);
                if (approverId == null) {
                    throw new BusinessException("未找到您的班主任，无法提交请假申请");
                }
                leaveRequestCreateDTO.setApproverId(approverId);
            }
            leaveRequestCreateDTO.setIsCourseRescheduled(false);
        } else if (USER_TYPE_TEACHER.equals(userType)) {
            leaveRequestCreateDTO.setApplicantType(APPLICANT_TYPE_TEACHER);
            leaveRequestCreateDTO.setApplicantId(userId);
            leaveRequestCreateDTO.setStudentId(null);
            if (leaveRequestCreateDTO.getApproverId() == null) {
                Long approverId = leaveApprovalMapper.getCollegeAdminByTeacherId(userId);
                if (approverId == null) {
                    throw new BusinessException("未找到您所在学院的管理员，无法提交请假申请");
                }
                leaveRequestCreateDTO.setApproverId(approverId);
            }
        } else {
            throw new BusinessException("用户类型不支持提交请假申请");
        }

        int result = leaveApprovalMapper.insertLeaveRequest(leaveRequestCreateDTO);
        if (result <= 0) {
            throw new BusinessException("请假申请失败");
        }
        Long id = leaveApprovalMapper.getLastInsertId();
        if (id == null || id <= 0) {
            throw new BusinessException("请假申请失败");
        }
        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalMapper.getLeaveRequestDetail(id);
        if (leaveRequestDetailVO == null) {
            throw new BusinessException("请假申请失败");
        }
        LeaveRequestResponseDTO responseDTO = convertToResponseDTO(leaveRequestDetailVO);
        try {
            Notification notification = new Notification();
            notification.setUserId(leaveRequestDetailVO.getApproverId());
            notification.setUserType(USER_TYPE_TEACHER);
            notification.setTitle("新的请假申请");
            notification.setContent("学生" + leaveRequestDetailVO.getApplicantName() + "提交了请假申请，请及时审批");
            notification.setRelatedId(leaveRequestDetailVO.getId());
            leaveNotificationService.notifyLeaveApply(notification);
        } catch (Exception e) {
            log.error("推送请假申请通知失败: id={}", id, e);
        }
        return responseDTO;
    }

    @Override
    @Transactional
    public Boolean approveLeaveRequest(Long id, LeaveApprovalDTO leaveApprovalDTO) {
        if (leaveApprovalDTO == null) {
            throw new IllegalArgumentException("审批参数不能为空");
        }
        if (id == null) {
            throw new IllegalArgumentException("请假申请ID不能为空");
        }
        if (leaveApprovalDTO.getAction() == null || leaveApprovalDTO.getAction().isEmpty()) {
            throw new IllegalArgumentException("请选择审批结果");
        }

        String userType = SecurityUtils.getCurrentUserType();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        leaveApprovalDTO.setId(id);
        leaveApprovalDTO.setApproverId(currentUserId);

        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalMapper.getLeaveRequestDetail(id);
        if (leaveRequestDetailVO == null) {
            throw new BusinessException("请假申请不存在");
        }

        if (!LEAVE_APPLY_STATUS_WAITING.equals(leaveRequestDetailVO.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        boolean hasPermission = false;
        if (USER_TYPE_ADMIN.equals(userType)) {
            if (APPLICANT_TYPE_TEACHER.equals(leaveRequestDetailVO.getApplicantType())) {
                // 管理员审批教师请假时，验证是否是指定的审批人
                if (currentUserId.equals(leaveRequestDetailVO.getApproverId())) {
                    hasPermission = true;
                } else {
                    log.warn("管理员越权审批教师请假: approverId={}, currentUserId={}, leaveId={}",
                            leaveRequestDetailVO.getApproverId(), currentUserId, id);
                    // 允许通过但记录日志
                    hasPermission = true;
                }
            } else {
                hasPermission = true;
            }
        } else if (USER_TYPE_TEACHER.equals(userType)) {
            if (APPLICANT_TYPE_STUDENT.equals(leaveRequestDetailVO.getApplicantType())) {
                hasPermission = currentUserId.equals(leaveRequestDetailVO.getApproverId());
            }
        }

        if (!hasPermission) {
            throw new BusinessException("您没有权限审批该请假申请");
        }

        int result = leaveApprovalMapper.approveLeaveRequest(leaveApprovalDTO);
        if (result <= 0) {
            throw new BusinessException("审批失败");
        }

        String targetStatus = APPROVAL_RESULT_APPROVED.equals(leaveApprovalDTO.getAction())
                ? LEAVE_APPLY_STATUS_APPROVED : LEAVE_APPLY_STATUS_REJECTED;
        int row = leaveApprovalMapper.updateLeaveRequestStatus(id, targetStatus);
        if (row <= 0) {
            throw new BusinessException("该申请已被处理，无法重复审批");
        }
        try {
            String recipientType = APPLICANT_TYPE_STUDENT.equals(leaveRequestDetailVO.getApplicantType())
                    ? USER_TYPE_STUDENT : USER_TYPE_TEACHER;
            Notification notification = new Notification();
            notification.setUserId(leaveRequestDetailVO.getApplicantId());
            notification.setUserType(recipientType);
            notification.setRelatedId(id);
            if (APPROVAL_RESULT_APPROVED.equals(leaveApprovalDTO.getAction())) {
                notification.setTitle("请假申请已审批");
                notification.setContent("您的请假申请已通过审批");
                leaveNotificationService.notifyLeaveApproved(notification);
            } else {
                notification.setTitle("请假申请已驳回");
                notification.setContent("您的请假申请已被驳回");
                leaveNotificationService.notifyLeaveRejected(notification);
            }
        } catch (Exception e) {
            log.error("推送审批结果通知失败: id={}", id, e);
        }
        return true;
    }

    @Override
    public Boolean cancelLeaveRequest(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("请假申请ID不能为空");
        }
        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalMapper.getLeaveRequestDetail(id);
        if (leaveRequestDetailVO == null || !LEAVE_APPLY_STATUS_WAITING.equals(leaveRequestDetailVO.getStatus())) {
            throw new BusinessException("请假申请不存在或者状态不是待审批状态");
        }
        int result = leaveApprovalMapper.updateLeaveRequestStatus(id, LEAVE_APPLY_STATUS_CANCELED);
        if (result <= 0) {
            throw new BusinessException("请假申请取消失败");
        }
        return true;
    }

    @Override
    public PageInfo<LeaveRequestListVO> getLeaveRequestList(Integer pageNum, Integer pageSize, String studentName, String leaveType, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<LeaveRequestListVO> leaveRequestListVOList = leaveApprovalMapper.getLeaveRequestList(studentName, leaveType, status);
        return PageInfo.of(leaveRequestListVOList);
    }

    @Override
    public LeaveRequestDetailVO getLeaveRequestDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("请假申请ID不能为空");
        }
        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalMapper.getLeaveRequestDetail(id);
        if (leaveRequestDetailVO == null) {
            throw NotFoundException.notFound("请假申请不存在", id);
        }
        return leaveRequestDetailVO;
    }

    @Override
    public List<MyLeaveRequestVO> getMyLeaveRequestList(String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userType = SecurityUtils.getCurrentUserType();
        if (userId == null) {
            throw new BusinessException("请用户先登陆");
        }
        String applicantType;
        if (USER_TYPE_STUDENT.equals(userType)) {
            applicantType = APPLICANT_TYPE_STUDENT;
        } else if (USER_TYPE_TEACHER.equals(userType)) {
            applicantType = APPLICANT_TYPE_TEACHER;
        } else {
            throw new BusinessException("用户类型不支持查询请假列表");
        }
        return leaveApprovalMapper.getMyLeaveRequestList(status, userId, applicantType);
    }

    @Override
    public PageInfo<PendingLeaveRequestVO> getPendingApprovalList(Integer pageNum, Integer pageSize) {
        String userType = SecurityUtils.getCurrentUserType();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Long approverId = null;
        if (USER_TYPE_TEACHER.equals(userType)) {
            approverId = currentUserId;
        }

        PageHelper.startPage(pageNum, pageSize);
        List<PendingLeaveRequestVO> pendingLeaveRequestVOList = leaveApprovalMapper.getPendingApprovalList(LEAVE_APPLY_STATUS_WAITING, approverId);
        return PageInfo.of(pendingLeaveRequestVOList);
    }

    @Override
    public List<LeaveApprovalLogVO> getLeaveApprovalLogList(Long leaveRequestId) {
        return leaveApprovalMapper.getLeaveApprovalLogList(leaveRequestId);
    }

    @Override
    public ApproverListVO getAvailableApprovers() {
        String userType = SecurityUtils.getCurrentUserType();
        Long userId = SecurityUtils.getCurrentUserId();

        List<ApproverVO> allAdmins = leaveApprovalMapper.getAllEnabledAdmins();
        Long defaultApproverId = null;
        ApproverVO defaultApprover = null;

        if (USER_TYPE_STUDENT.equals(userType)) {
            defaultApproverId = leaveApprovalMapper.getHeadTeacherByStudentId(userId);
            if (defaultApproverId != null) {
                defaultApprover = leaveApprovalMapper.getTeacherApproverById(defaultApproverId);
            }
        } else if (USER_TYPE_TEACHER.equals(userType)) {
            defaultApproverId = leaveApprovalMapper.getCollegeAdminByTeacherId(userId);
        }

        List<ApproverVO> approvers = new ArrayList<>();
        if (defaultApprover != null) {
            approvers.add(defaultApprover);
        }
        if (allAdmins != null) {
            approvers.addAll(allAdmins);
        }

        ApproverListVO result = new ApproverListVO();
        result.setDefaultApproverId(defaultApproverId);
        result.setApprovers(approvers);
        return result;
    }

    private LeaveRequestResponseDTO convertToResponseDTO(LeaveRequestDetailVO leaveRequestDetailVO) {
        LeaveRequestResponseDTO leaveRequestResponseDTO = new LeaveRequestResponseDTO();
        leaveRequestResponseDTO.setId(leaveRequestDetailVO.getId());
        leaveRequestResponseDTO.setApplicantType(leaveRequestDetailVO.getApplicantType());
        leaveRequestResponseDTO.setApplicantId(leaveRequestDetailVO.getApplicantId());
        leaveRequestResponseDTO.setApplicantName(leaveRequestDetailVO.getApplicantName());
        leaveRequestResponseDTO.setApproverId(leaveRequestDetailVO.getApproverId());
        leaveRequestResponseDTO.setApproverName(leaveRequestDetailVO.getApproverName());
        leaveRequestResponseDTO.setStudentId(leaveRequestDetailVO.getStudentId());
        leaveRequestResponseDTO.setStudentName(leaveRequestDetailVO.getStudentName());
        leaveRequestResponseDTO.setLeaveType(leaveRequestDetailVO.getLeaveType());
        leaveRequestResponseDTO.setStartTime(leaveRequestDetailVO.getStartTime());
        leaveRequestResponseDTO.setEndTime(leaveRequestDetailVO.getEndTime());
        leaveRequestResponseDTO.setReason(leaveRequestDetailVO.getReason());
        leaveRequestResponseDTO.setStatus(leaveRequestDetailVO.getStatus());
        leaveRequestResponseDTO.setIsCourseRescheduled(leaveRequestDetailVO.getIsCourseRescheduled());
        try {
            if (leaveRequestDetailVO.getCreateTime() != null && !leaveRequestDetailVO.getCreateTime().isEmpty()) {
                leaveRequestResponseDTO.setCreateTime(LocalDateTime.parse(leaveRequestDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (leaveRequestDetailVO.getUpdateTime() != null && !leaveRequestDetailVO.getUpdateTime().isEmpty()) {
                leaveRequestResponseDTO.setUpdateTime(LocalDateTime.parse(leaveRequestDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            throw new BusinessException("TIME_PARSE_ERROR", "时间解析错误", e);
        }
        return leaveRequestResponseDTO;
    }
}