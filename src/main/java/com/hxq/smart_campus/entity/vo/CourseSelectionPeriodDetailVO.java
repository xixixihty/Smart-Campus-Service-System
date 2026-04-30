package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSelectionPeriodDetailVO {
    private Long id;                // 时间段ID
    private Long semesterId;        // 学期ID
    private String semesterName;    // 学期名称
    private LocalDateTime startTime; // 选课开始时间
    private LocalDateTime endTime;   // 选课结束时间
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
