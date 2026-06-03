package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MakeupExamCreateDTO {
    private Long scoreEntryId;      // 成绩记录ID，必填
    private LocalDate examDate;     // 补考日期，必填
    private String location;        // 补考地点，必填
}