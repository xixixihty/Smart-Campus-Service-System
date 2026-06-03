package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.CourseRescheduleCreateDTO;
import com.hxq.smart_campus.entity.pojo.CourseReschedule;
import com.hxq.smart_campus.entity.vo.CourseRescheduleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseRescheduleMapper {

    int insert(@Param("dto") CourseRescheduleCreateDTO.RescheduleItem item,
               @Param("leaveRequestId") Long leaveRequestId,
               @Param("teacherId") Long teacherId);

    int confirmReschedule(@Param("id") Long id);

    int cancelReschedule(@Param("id") Long id);

    List<CourseRescheduleVO> getRescheduleListByLeaveRequest(@Param("leaveRequestId") Long leaveRequestId);

    List<CourseRescheduleVO> getRescheduleListByTeacher(@Param("teacherId") Long teacherId);

    CourseRescheduleVO getRescheduleDetail(@Param("id") Long id);

    List<CourseRescheduleVO> getTeacherCourseSchedules(@Param("teacherId") Long teacherId);

    List<CourseRescheduleVO> getPendingList();

    int approveReschedule(@Param("id") Long id);

    int rejectReschedule(@Param("id") Long id, @Param("reason") String reason);

    /**
     * 将调课变更应用到排课表
     */
    int applyRescheduleToSchedule(@Param("scheduleId") Long scheduleId,
                                  @Param("weekDay") Integer weekDay,
                                  @Param("startSection") Integer startSection,
                                  @Param("endSection") Integer endSection,
                                  @Param("classroomId") Long classroomId);
}