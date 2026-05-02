package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreEntry {
    private Long id;
    private Long courseId;
    private Long studentId;
    private Long semesterId;
    private BigDecimal usualScore;
    private BigDecimal finalScore;
    private BigDecimal totalScore;
    private BigDecimal makeupScore;
    private BigDecimal scorePoint;
    private String examStatus;
    private Long makeupExamId;
}
