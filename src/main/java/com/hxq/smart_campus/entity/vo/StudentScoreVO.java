package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StudentScoreVO {
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long classId;
    private String className;
    private List<CourseScoreItemVO> courseScores;
    private BigDecimal averageScore;
    private BigDecimal totalScore;
    private BigDecimal gpa;
}