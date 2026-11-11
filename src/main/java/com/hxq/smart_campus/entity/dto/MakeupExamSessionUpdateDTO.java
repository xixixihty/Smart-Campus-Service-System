package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 更新补考场次请求
 */
@Data
public class MakeupExamSessionUpdateDTO {
    private Long id;
    private LocalDate examDate;
    private LocalTime examStartTime;
    private LocalTime examEndTime;
    private String location;
    private Integer maxCapacity;
}