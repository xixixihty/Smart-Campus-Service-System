package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class ScoreStatisticsQueryDTO {
    private Long semesterId;

    private StatisticsDimensionEnum dimension;

    private Long courseId;
    private Long studentId;
    private Long classId;
    private Long majorId;
    private Long collegeId;
}