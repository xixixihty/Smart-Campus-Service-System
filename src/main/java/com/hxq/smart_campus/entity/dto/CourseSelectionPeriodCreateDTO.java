package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSelectionPeriodCreateDTO {
    private Long semesterId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
