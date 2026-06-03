package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveRequestCreateDTO {
    private String applicantType;
    private Long applicantId;
    private Long approverId;
    private Long studentId;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Boolean isCourseRescheduled;
}