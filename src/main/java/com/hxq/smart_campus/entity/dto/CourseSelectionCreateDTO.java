package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class CourseSelectionCreateDTO {
    private Long studentId;         // 学生ID，必填
    private Long courseId;          // 课程ID，必填
    private Long semesterId;        // 学期ID，必填
}
