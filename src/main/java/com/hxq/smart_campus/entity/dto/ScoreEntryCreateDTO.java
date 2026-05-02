package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreEntryCreateDTO {
    private Long courseId;          // 课程ID，必填
    private Long studentId;         // 学生ID，必填
    private Long semesterId;        // 学期ID，必填
    private BigDecimal usualScore;  // 平时成绩，可选
    private BigDecimal finalScore;  // 期末成绩，可选
    private String examStatus;      // 考试状态，可选
}
