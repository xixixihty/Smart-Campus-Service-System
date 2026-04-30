package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseUpdateDTO {
    private Long id;                // 课程ID，必填
    private String courseName;      // 课程名称，可选
    private BigDecimal credit;      // 学分，可选
    private Integer hours;          // 总学时，可选
    private String type;            // 课程类型，可选
    private String status;          // 状态，可选
    private Short capacity;        // 课程容量
}
