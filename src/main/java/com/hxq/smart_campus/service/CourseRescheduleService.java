package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.dto.CourseRescheduleCreateDTO;
import com.hxq.smart_campus.entity.vo.CourseRescheduleVO;

import java.util.List;

public interface CourseRescheduleService {

    List<CourseRescheduleVO> createReschedule(CourseRescheduleCreateDTO dto);

    void confirmReschedule(Long id);

    void cancelReschedule(Long id);

    List<CourseRescheduleVO> getRescheduleListByLeaveRequest(Long leaveRequestId);

    List<CourseRescheduleVO> getRescheduleListByTeacher();

    CourseRescheduleVO getRescheduleDetail(Long id);

    List<CourseRescheduleVO> getTeacherCourseSchedules();

    List<CourseRescheduleVO> getPendingList();

    void approveReschedule(Long id);

    void rejectReschedule(Long id, String reason);
}