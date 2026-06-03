package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MyMakeupExamVO {
    private Long id;                // 补考ID
    private String courseName;      // 课程名称
    private LocalDate examDate;     // 补考日期
    private String location;        // 补考地点
    private String status;          // 状态
}