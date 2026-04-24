package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterCreateDTO {
    private String name;            // 学期名称，必填
    private LocalDate startDate;    // 开学日期，必填
    private LocalDate endDate;      // 结束日期，必填
    private Boolean isCurrent;      // 是否当前学期，可选
    private String status;          // 状态，可选
}
