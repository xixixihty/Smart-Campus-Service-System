package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.LeaveApprovalDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestCreateDTO;
import com.hxq.smart_campus.entity.dto.LeaveRequestResponseDTO;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.exception.BusinessException;
import com.hxq.smart_campus.exception.NotFoundException;
import com.hxq.smart_campus.mapper.LeaveApprovalMapper;
import com.hxq.smart_campus.service.LeaveApprovalService;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveApprovalServiceImpl implements LeaveApprovalService {
    private final LeaveApprovalMapper leaveApprovalMapper;

    /**
     * 提交请假申请
     * @param leaveRequestCreateDTO
     * @return
     */
    @Override
    public LeaveRequestResponseDTO insertLeaveRequest(LeaveRequestCreateDTO leaveRequestCreateDTO) {
        // 校验参数
        if (leaveRequestCreateDTO == null) {
            throw new IllegalArgumentException("请假申请参数不能为空");
        }
        if (leaveRequestCreateDTO.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
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
        int result = leaveApprovalMapper.insertLeaveRequest(leaveRequestCreateDTO);
        if (result <= 0) {
            throw new BusinessException("请假申请失败");
        }
        // 获取最新插入的请假申请ID
        Long id = leaveApprovalMapper.getLastInsertId();
        if (id <= 0) {
            throw new BusinessException("请假申请失败");
        }
        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalMapper.getLeaveRequestDetail(id);
        if (leaveRequestDetailVO == null) {
            throw new BusinessException("请假申请失败");
        }
        // 构建响应DTO
        return convertToResponseDTO(leaveRequestDetailVO);
    }

    /**
     * 审批请假申请
     * @param leaveApprovalDTO
     * @return
     */
    @Override
    @Transactional
    public Boolean approveLeaveRequest(Long id, LeaveApprovalDTO leaveApprovalDTO) {
        // 校验参数
        if (leaveApprovalDTO == null) {
            throw new IllegalArgumentException("审批参数不能为空");
        }
        if (leaveApprovalDTO.getId() <= 0) {
            throw new IllegalArgumentException("请假申请ID不能为空");
        }
        if (leaveApprovalDTO.getResult() == null || leaveApprovalDTO.getResult().isEmpty()) {
            throw new IllegalArgumentException("请选择审批结果");
        }
        String userType = SecurityUtils.getCurrentUserType();
        if (!USER_TYPE_TEACHER.equals(userType)) {
            throw new BusinessException("用户类型错误");
        }
        leaveApprovalDTO.setId(id);
        leaveApprovalDTO.setApproverId(SecurityUtils.getCurrentUserId());
        // 查询请假申请
        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalMapper.getLeaveRequestDetail(leaveApprovalDTO.getId());
        if (leaveRequestDetailVO == null) {
            throw new BusinessException("请假申请不存在");
        }
        // 判断请假的状态是否是待审批状态
        if (!LEAVE_APPLY_STATUS_WAITING.equals(leaveRequestDetailVO.getStatus())) {
            throw new BusinessException("该申请以处理");
        }
        int result = leaveApprovalMapper.approveLeaveRequest(leaveApprovalDTO);
        if (result <= 0) {
            throw new BusinessException("审批失败");
        }
        // 更新请假申请状态为已批准或已拒绝
        String targetStatus = "批准".equals(leaveApprovalDTO.getResult()) ? LEAVE_APPLY_STATUS_APPROVED : LEAVE_APPLY_STATUS_REJECTED;
        int row = leaveApprovalMapper.updateLeaveRequestStatus(leaveApprovalDTO.getId(), targetStatus);
        if (row <= 0) {
            throw new BusinessException("该申请已被处理，无法重复审批");
        }
        return true;
    }

    /**
     * 取消请假申请
     * @param id
     * @return
     */
    @Override
    public Boolean cancelLeaveRequest(Long id) {
        // 校验参数
        if (id == null) {
            throw new IllegalArgumentException("请假申请ID不能为空");
        }
        // 判断请假申请是否存在，并且状态是不是待审批状态
        LeaveRequestDetailVO leaveRequestDetailVO = leaveApprovalMapper.getLeaveRequestDetail(id);
        if (leaveRequestDetailVO == null || !LEAVE_APPLY_STATUS_WAITING.equals(leaveRequestDetailVO.getStatus())) {
            throw new BusinessException("请假申请不存在或者状态不是待审批状态");
        }
        // 更新请假申请状态为已取消
        int result = leaveApprovalMapper.updateLeaveRequestStatus(id, LEAVE_APPLY_STATUS_CANCELED);
        if (result <= 0) {
            throw new BusinessException("请假申请取消失败");
        }
        return true;
    }

    /**
     * 获取请假申请列表
     * @param pageNum
     * @param pageSize
     * @param studentId
     * @param status
     * @return
     */
    @Override
    public PageInfo<LeaveRequestListVO> getLeaveRequestList(Integer pageNum, Integer pageSize, Long studentId, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<LeaveRequestListVO> leaveRequestListVOList = leaveApprovalMapper.getLeaveRequestList(studentId, status);
        return PageInfo.of(leaveRequestListVOList);
    }

    /**
     * 获取请假申请详情
     * @param id
     * @return
     */
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

    /**
     * 获取我的请假申请列表
     * @param status
     * @return
     */
    @Override
    public List<MyLeaveRequestVO> getMyLeaveRequestList(String status) {
        // 从登陆信息中获取用户信息
        Long studentId = SecurityUtils.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("请用户先登陆");
        }
        List<MyLeaveRequestVO> myLeaveRequestVOList = leaveApprovalMapper.getMyLeaveRequestList(status, studentId);
        return myLeaveRequestVOList;
    }

    /**
     * 获取待审批请假申请列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<PendingLeaveRequestVO> getPendingApprovalList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PendingLeaveRequestVO> pendingLeaveRequestVOList = leaveApprovalMapper.getPendingApprovalList(LEAVE_APPLY_STATUS_WAITING);
        return PageInfo.of(pendingLeaveRequestVOList);
    }
    /**
     * 获取请假申请审批日志列表
     * @param leaveRequestId
     * @return
     */
    @Override
    public List<LeaveApprovalLogVO> getLeaveApprovalLogList(Long leaveRequestId) {
        return leaveApprovalMapper.getLeaveApprovalLogList(leaveRequestId);
    }
    private LeaveRequestResponseDTO convertToResponseDTO(LeaveRequestDetailVO leaveRequestDetailVO) {
        LeaveRequestResponseDTO leaveRequestResponseDTO = new LeaveRequestResponseDTO();
        leaveRequestResponseDTO.setId(leaveRequestDetailVO.getId());
        leaveRequestResponseDTO.setStudentId(leaveRequestDetailVO.getStudentId());
        leaveRequestResponseDTO.setStudentName(leaveRequestDetailVO.getStudentName());
        leaveRequestResponseDTO.setLeaveType(leaveRequestDetailVO.getLeaveType());
        leaveRequestResponseDTO.setStartTime(leaveRequestDetailVO.getStartTime());
        leaveRequestResponseDTO.setEndTime(leaveRequestDetailVO.getEndTime());
        leaveRequestResponseDTO.setReason(leaveRequestDetailVO.getReason());
        leaveRequestResponseDTO.setStatus(leaveRequestDetailVO.getStatus());
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
