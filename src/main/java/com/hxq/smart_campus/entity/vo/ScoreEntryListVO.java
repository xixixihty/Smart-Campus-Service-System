package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreEntryListVO {
    private Long id;                // 成绩ID
    private String courseName;      // 课程名称
    private String studentName;     // 学生姓名
    private String studentNo;       // 学号
    private String semesterName;    // 学期名称
    private BigDecimal usualScore;  // 平时成绩
    private BigDecimal finalScore;  // 期末成绩
    private BigDecimal totalScore;  // 总评成绩
    private BigDecimal scorePoint;  // 绩点
    private BigDecimal credit;      // 学分
    private String examStatus;      // 考试状态
}
