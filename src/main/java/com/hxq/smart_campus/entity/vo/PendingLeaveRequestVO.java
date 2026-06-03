package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class PendingLeaveRequestVO {
    private Long id;
    private String applicantType;
    private String applicantName;
    private String studentName;
    private String leaveType;
    private String startTime;
    private String endTime;
    private String reason;
    private String createTime;
}