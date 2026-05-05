package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveApprovalLogDTO {
    private Long id;                // 审批记录ID
    private Long leaveRequestId;    // 请假申请ID
    private Long approverId;        // 审批人ID
    private String approverName;    // 审批人姓名
    private LocalDateTime approveTime; // 审批时间
    private String result;          // 审批结果
    private String comment;         // 审批意见
}
