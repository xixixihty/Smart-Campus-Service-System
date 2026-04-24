package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseCreateDTO {
    private String courseCode;      // 课程代码，必填
    private String courseName;      // 课程名称，必填
    private BigDecimal credit;      // 学分，必填
    private Integer hours;          // 总学时，必填
    private String type;            // 课程类型，必填
    private String status;          // 状态，可选
}
