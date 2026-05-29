package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentCourseVO {
    private Long courseId;
    private String courseName;
    private BigDecimal credit;
    private String type;
    private String teacherName;
    private String classroomName;
    private Integer weekDay;
    private Integer startSection;
    private Integer endSection;
    private String weekRange;
    private String classNames;
    private String source;
    private String semesterName;
    private BigDecimal score;
    private BigDecimal scorePoint;
}