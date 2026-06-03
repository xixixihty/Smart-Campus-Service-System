package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LeaveRequestDetailVO {
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
    private String createTime;
    private String updateTime;
    private List<LeaveApprovalLogVO> approvalLogs;
}