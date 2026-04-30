package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSelectionPeriodUpdateDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
