package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreEntryCreateDTO {
    private Long courseId;
    private Long studentId;
    private Long semesterId;
    private BigDecimal usualScore;
    private BigDecimal finalScore;
    private String examStatus;
    private BigDecimal totalScore;
    private BigDecimal scorePoint;
}
