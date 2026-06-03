package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveRequestResponseDTO {
    private Long id;
    private String applicantType;
    private Long applicantId;
    private String applicantName;
    private Long approverId;
    private String approverName;
    private Long studentId;
    private String studentName;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private String status;
    private Integer isCourseRescheduled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}