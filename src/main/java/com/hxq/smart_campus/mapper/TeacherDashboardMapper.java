package com.hxq.smart_campus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TeacherDashboardMapper {

    Integer getTeachingCourseCount(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    Integer getTeachingClassCount(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    Integer getTeachingStudentCount(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    Integer getPendingLeaveCount(@Param("teacherId") Long teacherId);

    Integer getPendingRescheduleCount(@Param("teacherId") Long teacherId);

    BigDecimal getScoreEntryRate(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    List<ClassStudentCountVO> getClassStudentDistribution(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);
}