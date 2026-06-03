package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class LeaveApprovalLogVO {
    private Long id;                // 审批记录ID
    private Long approverId;        // 审批人ID
    private String approverName;    // 审批人姓名
    private String action;          // 审批动作：通过/驳回
    private String comment;         // 审批意见
    private String createTime;      // 审批时间（格式化显示）
}
