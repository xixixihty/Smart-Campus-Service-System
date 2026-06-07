package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.dto.CourseRescheduleCreateDTO;
import com.hxq.smart_campus.entity.vo.CourseRescheduleVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleDetailVO;
import com.hxq.smart_campus.exception.BusinessException;
import com.hxq.smart_campus.mapper.CourseRescheduleMapper;
import com.hxq.smart_campus.mapper.CourseScheduleMapper;
import com.hxq.smart_campus.service.CourseRescheduleService;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseRescheduleServiceImpl implements CourseRescheduleService {

    private final CourseRescheduleMapper courseRescheduleMapper;
    private final CourseScheduleMapper courseScheduleMapper;

    @Override
    @Transactional
    public List<CourseRescheduleVO> createReschedule(CourseRescheduleCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("调课参数不能为空");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("请至少选择一门课程进行调课");
        }

        Long teacherId = SecurityUtils.getCurrentUserId();

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
        }

        for (CourseRescheduleCreateDTO.RescheduleItem item : dto.getItems()) {
            // A3: 将顶层调课原因赋到每个调课项
            if (dto.getReason() != null) {
                item.setReason(dto.getReason());
            }
            int row = courseRescheduleMapper.insert(item, dto.getLeaveRequestId(), teacherId);
            if (row <= 0) {
                throw new BusinessException("调课创建失败");
            }
        }

        return courseRescheduleMapper.getRescheduleListByTeacher(teacherId);
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
        if (!RESCHEDULE_STATUS_PENDING.equals(vo.getStatus())) {
            throw new BusinessException("只能取消待审批的调课记录");
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
        Long adminId = SecurityUtils.getCurrentUserId();
        int row = courseRescheduleMapper.approveReschedule(id, adminId);
        if (row <= 0) {
            throw new BusinessException("调课审批失败");
        }
        // 将调课变更应用到排课表（同步更新时间、周次、教室）
        courseRescheduleMapper.applyRescheduleToSchedule(
                vo.getCourseScheduleId(),
                vo.getNewWeekDay(),
                vo.getNewStartSection(),
                vo.getNewEndSection(),
                vo.getNewWeekRange(),
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