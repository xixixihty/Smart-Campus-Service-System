package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MyMakeupExamVO {
    private Long id;                // 补考ID
    private String courseName;      // 课程名称
    private LocalDate examDate;     // 补考日期
    private LocalTime startTime;    // 开始时间
    private LocalTime endTime;      // 结束时间
    private String status;          // 状态
}
