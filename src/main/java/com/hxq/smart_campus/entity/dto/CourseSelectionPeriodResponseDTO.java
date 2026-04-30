package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSelectionPeriodResponseDTO {
    private Long id;                // 时间段ID
    private Long semesterId;        // 学期ID
    private String semesterName;    // 学期名称
    private LocalDateTime startTime; // 选课开始时间
    private LocalDateTime endTime;   // 选课结束时间
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
