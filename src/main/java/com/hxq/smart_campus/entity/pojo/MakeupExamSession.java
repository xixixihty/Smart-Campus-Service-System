package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 补考场次实体（以课程为粒度组织补考）
 */
@Data
public class MakeupExamSession {
    private Long id;
    private Long courseId;
    private Long semesterId;
    private LocalDate examDate;
    private LocalTime examStartTime;
    private LocalTime examEndTime;
    private String location;
    private Integer maxCapacity;
    private Integer enrolledCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}