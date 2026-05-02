package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成绩统计VO
 * 用于封装成绩统计分析结果
 */
@Data
public class ScoreEntryForStatisticsVO {
    /** 成绩记录ID */
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学号 */
    private String studentNo;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    private String className;

    /** 专业ID */
    private Long majorId;

    /** 专业名称 */
    private String majorName;

    /** 学院ID */
    private Long collegeId;

    /** 学院名称 */
    private String collegeName;

    /** 课程ID */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

    /** 平时成绩 */
    private BigDecimal usualScore;

    /** 期末成绩 */
    private BigDecimal finalScore;

    /** 总评成绩 */
    private BigDecimal totalScore;
}
