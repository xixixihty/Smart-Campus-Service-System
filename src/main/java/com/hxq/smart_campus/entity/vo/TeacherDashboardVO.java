package com.hxq.smart_campus.entity.vo;

import com.hxq.smart_campus.mapper.ClassStudentCountVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TeacherDashboardVO {
    private String semesterName;
    private Integer teachingCourseCount;
    private Integer teachingClassCount;
    private Integer teachingStudentCount;
    private Integer pendingLeaveCount;
    private Integer pendingRescheduleCount;
    private BigDecimal scoreEntryRate;
    private List<ClassStudentCountVO> classStudentDistribution;
    private List<ScoreDistributionItem> scoreDistribution;
    private List<java.util.Map<String, Object>> leaveTrend;
}