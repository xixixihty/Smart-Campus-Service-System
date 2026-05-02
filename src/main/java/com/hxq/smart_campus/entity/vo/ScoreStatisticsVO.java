package com.hxq.smart_campus.entity.vo;

import com.hxq.smart_campus.entity.dto.StatisticsDimensionEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ScoreStatisticsVO {
    private StatisticsDimensionEnum dimension;
    private String dimensionName;

    private Integer totalCount;
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;

    private Integer excellentCount;
    private Integer goodCount;
    private Integer passCount;
    private Integer failCount;

    private BigDecimal excellentRate;
    private BigDecimal goodRate;
    private BigDecimal passRate;

    private Long semesterId;
    private String semesterName;

    private Long courseId;
    private String courseName;

    private Long studentId;
    private String studentName;
    private String studentNo;

    private Long classId;
    private String className;

    private Long majorId;
    private String majorName;

    private Long collegeId;
    private String collegeName;

    private Map<String, Integer> scoreDistribution;
}