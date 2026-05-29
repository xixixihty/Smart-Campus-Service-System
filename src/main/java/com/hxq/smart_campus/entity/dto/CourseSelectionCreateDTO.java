package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class CourseSelectionCreateDTO {
    private Long studentId;
    private Long courseId;
    private Long semesterId;
    private String status;
}