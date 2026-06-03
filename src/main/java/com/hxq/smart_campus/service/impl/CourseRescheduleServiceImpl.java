package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.dto.CourseRescheduleCreateDTO;
import com.hxq.smart_campus.entity.vo.CourseRescheduleVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleDetailVO;
import com.hxq.smart_campus.entity.vo.LeaveRequestDetailVO;
import com.hxq.smart_campus.exception.BusinessException;
import com.hxq.smart_campus.mapper.CourseRescheduleMapper;
import com.hxq.smart_campus.mapper.CourseScheduleMapper;
import com.hxq.smart_campus.mapper.LeaveApprovalMapper;
import com.hxq.smart_campus.service.CourseRescheduleService;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseRescheduleServiceImpl implements CourseRescheduleService {

    private final CourseRescheduleMapper courseRescheduleMapper;
    private final LeaveApprovalMapper leaveApprovalMapper;
    private final CourseScheduleMapper courseScheduleMapper;

    @Override
    @Transactional
    public List<CourseRescheduleVO> createReschedule(CourseRescheduleCreateDTO dto) {
        if (dto == null || dto.getLeaveRequestId() == null) {
            throw new IllegalArgumentException("调课参数不能为空");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("请至少选择一门课程进行调课");
        }

        LeaveRequestDetailVO leave = leaveApprovalMapper.getLeaveRequestDetail(dto.getLeaveRequestId());
        if (leave == null) {
            throw new BusinessException("请假申请不存在");
        }
        if (!LEAVE_APPLY_STATUS_APPROVED.equals(leave.getStatus())) {
            throw new BusinessException("请假申请未审批通过，无法调课");
        }
        if (!APPLICANT_TYPE_TEACHER.equals(leave.getApplicantType())) {
            throw new BusinessException("仅教师可以调课");
        }

        Long teacherId = SecurityUtils.getCurrentUserId();
        if (!teacherId.equals(leave.getApplicantId())) {
            throw new BusinessException("只能对自己的请假进行调课");
        }

        for (CourseRescheduleCreateDTO.RescheduleItem item : dto.getItems()) {
            if (item.getNewWeekDay() == null || item.getNewStartSection() == null || item.getNewEndSection() == null) {
                throw new IllegalArgumentException("调课时间参数不完整");
            }
        }

        // 冲突检测：检查新时间是否与已有排课冲突
        for (CourseRescheduleCreateDTO.RescheduleItem item : dto.getItems()) {
            CourseScheduleDetailVO schedule = courseScheduleMapper.getCourseScheduleDetail(item.getCourseScheduleId());
            if (schedule == null) {
                throw new BusinessException("排课记录不存在: " + item.getCourseScheduleId());
            }
            // 检查教师冲突（排除当前排课）
            List<CourseScheduleDetailVO> teacherConflicts = courseScheduleMapper.checkTeacherConflict(
                    schedule.getSemesterId(), teacherId, item.getNewWeekDay(),
                    item.getNewStartSection(), item.getNewEndSection(),
                    schedule.getWeekRange(), item.getCourseScheduleId());
            if (teacherConflicts != null && !teacherConflicts.isEmpty()) {
                throw new BusinessException("教师在该时间段已有排课，请选择其他时间");
            }
            // 检查教室冲突
            if (item.getNewClassroomId() != null) {
                List<CourseScheduleDetailVO> roomConflicts = courseScheduleMapper.checkClassroomConflict(
                        schedule.getSemesterId(), item.getNewClassroomId(), item.getNewWeekDay(),
                        item.getNewStartSection(), item.getNewEndSection(),
                        schedule.getWeekRange(), item.getCourseScheduleId());
                if (roomConflicts != null && !roomConflicts.isEmpty()) {
                    throw new BusinessException("目标教室在该时间段已被占用，请选择其他教室");
                }
            }
            // 校验调课日期必须在请假时间范围内
            if (leave.getStartTime() != null && leave.getEndTime() != null) {
                if (item.getNewWeekDay() != null) {
                    int leaveStartDayOfWeek = leave.getStartTime().getDayOfWeek().getValue();
                    int leaveEndDayOfWeek = leave.getEndTime().getDayOfWeek().getValue();
                    int newDay = item.getNewWeekDay();
                    // 周一=1 ... 周日=7，java.time的DayOfWeek也是周一=1
                    boolean inRange;
                    if (leaveStartDayOfWeek <= leaveEndDayOfWeek) {
                        inRange = newDay >= leaveStartDayOfWeek && newDay <= leaveEndDayOfWeek;
                    } else {
                        inRange = newDay >= leaveStartDayOfWeek || newDay <= leaveEndDayOfWeek;
                    }
                    if (!inRange) {
                        throw new BusinessException("调课日期超出了请假时间范围，请假日期为每周"
                                + leaveStartDayOfWeek + "至" + leaveEndDayOfWeek);
                    }
                }
            }
        }

        List<CourseRescheduleVO> results = new ArrayList<>();
        for (CourseRescheduleCreateDTO.RescheduleItem item : dto.getItems()) {
            int row = courseRescheduleMapper.insert(item, dto.getLeaveRequestId(), teacherId);
            if (row <= 0) {
                throw new BusinessException("调课创建失败");
            }
        }

        return courseRescheduleMapper.getRescheduleListByLeaveRequest(dto.getLeaveRequestId());
    }

    @Override
    public void confirmReschedule(Long id) {
        CourseRescheduleVO vo = courseRescheduleMapper.getRescheduleDetail(id);
        if (vo == null) {
            throw new BusinessException("调课记录不存在");
        }
        if (!RESCHEDULE_STATUS_PENDING.equals(vo.getStatus())) {
            throw new BusinessException("该调课记录已处理");
        }
        courseRescheduleMapper.confirmReschedule(id);
    }

    @Override
    public void cancelReschedule(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserType = SecurityUtils.getCurrentUserType();
        CourseRescheduleVO vo = courseRescheduleMapper.getRescheduleDetail(id);
        if (vo == null) {
            throw new BusinessException("调课记录不存在");
        }
        // 管理员可以取消任意调课，教师只能取消自己的调课
        if (!USER_TYPE_ADMIN.equals(currentUserType) && !currentUserId.equals(vo.getTeacherId())) {
            throw new BusinessException("无权取消该调课记录");
        }
        courseRescheduleMapper.cancelReschedule(id);
    }

    @Override
    public List<CourseRescheduleVO> getRescheduleListByLeaveRequest(Long leaveRequestId) {
        return courseRescheduleMapper.getRescheduleListByLeaveRequest(leaveRequestId);
    }

    @Override
    public List<CourseRescheduleVO> getRescheduleListByTeacher() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        return courseRescheduleMapper.getRescheduleListByTeacher(teacherId);
    }

    @Override
    public CourseRescheduleVO getRescheduleDetail(Long id) {
        CourseRescheduleVO vo = courseRescheduleMapper.getRescheduleDetail(id);
        if (vo == null) {
            throw new BusinessException("调课记录不存在");
        }
        return vo;
    }

    @Override
    public List<CourseRescheduleVO> getTeacherCourseSchedules() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        return courseRescheduleMapper.getTeacherCourseSchedules(teacherId);
    }

    @Override
    public List<CourseRescheduleVO> getPendingList() {
        return courseRescheduleMapper.getPendingList();
    }

    @Override
    @Transactional
    public void approveReschedule(Long id) {
        CourseRescheduleVO vo = courseRescheduleMapper.getRescheduleDetail(id);
        if (vo == null) {
            throw new BusinessException("调课记录不存在");
        }
        if (!RESCHEDULE_STATUS_PENDING.equals(vo.getStatus())) {
            throw new BusinessException("该调课记录已处理");
        }
        int row = courseRescheduleMapper.approveReschedule(id);
        if (row <= 0) {
            throw new BusinessException("调课审批失败");
        }
        // 将调课变更应用到排课表
        courseRescheduleMapper.applyRescheduleToSchedule(
                vo.getCourseScheduleId(),
                vo.getNewWeekDay(),
                vo.getNewStartSection(),
                vo.getNewEndSection(),
                vo.getNewClassroomId()
        );
    }

    @Override
    @Transactional
    public void rejectReschedule(Long id, String reason) {
        CourseRescheduleVO vo = courseRescheduleMapper.getRescheduleDetail(id);
        if (vo == null) {
            throw new BusinessException("调课记录不存在");
        }
        if (!RESCHEDULE_STATUS_PENDING.equals(vo.getStatus())) {
            throw new BusinessException("该调课记录已处理");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("驳回原因不能为空");
        }
        int row = courseRescheduleMapper.rejectReschedule(id, reason);
        if (row <= 0) {
            throw new BusinessException("驳回调课失败");
        }
    }
}