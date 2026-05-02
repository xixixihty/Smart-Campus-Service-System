package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreEntryUpdateDTO {
    private Long id;                // 成绩ID，必填
    private BigDecimal usualScore;  // 平时成绩，可选
    private BigDecimal finalScore;  // 期末成绩，可选
    private BigDecimal makeupScore; // 补考成绩，可选
    private String examStatus;      // 考试状态，可选
}
