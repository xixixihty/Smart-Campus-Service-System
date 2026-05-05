package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveRequestCreateDTO {
    private Long id;
    private Long studentId;         // 学生ID，必填
    private String leaveType;       // 请假类型，必填
    private LocalDateTime startTime; // 开始时间，必填
    private LocalDateTime endTime;   // 结束时间，必填
    private String reason;          // 请假事由，必填
}
