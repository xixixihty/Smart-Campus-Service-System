package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 补考场次列表项
 */
@Data
public class MakeupExamSessionListVO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Long semesterId;
    private String semesterName;
    private LocalDate examDate;
    private LocalTime examStartTime;
    private LocalTime examEndTime;
    private String location;
    private Integer maxCapacity;
    private Integer enrolledCount;
    private String status;
    private String createTime;
}