package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseSelection {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long semesterId;
    private String status;
    private BigDecimal score;
    private BigDecimal scorePoint;
}
