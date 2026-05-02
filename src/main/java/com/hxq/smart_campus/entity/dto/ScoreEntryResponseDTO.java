package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScoreEntryResponseDTO {
    private Long id;                // 成绩ID
    private Long courseId;          // 课程ID
    private String courseName;      // 课程名称
    private Long studentId;         // 学生ID
    private String studentName;     // 学生姓名
    private String studentNo;       // 学号
    private Long semesterId;        // 学期ID
    private String semesterName;    // 学期名称
    private BigDecimal usualScore;  // 平时成绩
    private BigDecimal finalScore;  // 期末成绩
    private BigDecimal totalScore;  // 总评成绩
    private BigDecimal makeupScore; // 补考成绩
    private BigDecimal scorePoint;  // 绩点
    private String examStatus;      // 考试状态
    private Long makeupExamId;      // 补考安排ID
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
