package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MakeupExamCreateDTO {
    private Long scoreEntryId;      // 成绩记录ID，必填
    private LocalDate examDate;     // 补考日期，必填
    private LocalTime startTime;    // 开始时间，必填
    private LocalTime endTime;      // 结束时间，必填
}
