package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseScoreItemVO {
    private Long courseId;
    private String courseName;
    private BigDecimal usualScore;
    private BigDecimal finalScore;
    private BigDecimal totalScore;
    private BigDecimal scorePoint;
    private String grade;
}