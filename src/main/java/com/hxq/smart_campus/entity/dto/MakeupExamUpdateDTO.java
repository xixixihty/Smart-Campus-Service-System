package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MakeupExamUpdateDTO {
    private Long id;                // 补考ID，必填
    private LocalDate examDate;     // 补考日期，可选
    private LocalTime startTime;    // 开始时间，可选
    private LocalTime endTime;      // 结束时间，可选
    private String status;          // 状态，可选
}
