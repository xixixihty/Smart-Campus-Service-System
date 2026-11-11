package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 补考场次详情（含学生列表）
 */
@Data
public class MakeupExamSessionDetailVO {
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
    private String updateTime;
    private List<MakeupExamStudentVO> students;
}