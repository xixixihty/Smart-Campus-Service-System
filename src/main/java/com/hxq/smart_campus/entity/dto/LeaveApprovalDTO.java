package com.hxq.smart_campus.entity.dto;


import lombok.Data;

@Data
public class LeaveApprovalDTO {
    private Long id;                // 请假申请ID，必填
    private Long approverId;        // 审批人ID，必填
    private String action;          // 审批动作（通过/驳回），必填
    private String comment;         // 审批意见，可选
}
