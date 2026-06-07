package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 学生工作台统计VO
 */
@Data
public class StudentDashboardVO {
    /** 已选课程数 */
    private Integer courseCount;
    /** 平均成绩 */
    private BigDecimal avgScore;
    /** 待审批请假数 */
    private Integer pendingLeaveCount;
    /** 今日课程数 */
    private Integer todayCourseCount;
}