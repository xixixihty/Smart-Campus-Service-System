package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherScoreStatsDTO {
    private String semesterName;
    private Integer totalStudents;
    private Integer recordedCount;
    private Integer unrecordedCount;
    private BigDecimal averageScore;
    private BigDecimal passRate;
}
