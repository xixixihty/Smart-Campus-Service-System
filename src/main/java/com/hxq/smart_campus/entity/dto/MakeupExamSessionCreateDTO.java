package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建补考场次请求
 */
@Data
public class MakeupExamSessionCreateDTO {
    private Long courseId;
    private Long semesterId;
    private LocalDate examDate;
    private LocalTime examStartTime;
    private LocalTime examEndTime;
    private String location;
    private Integer maxCapacity;
}