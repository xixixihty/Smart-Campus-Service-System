package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class LeaveRequestListVO {
    private Long id;
    private String applicantType;
    private String applicantName;
    private String approverName;
    private String studentName;
    private String leaveType;
    private String startTime;
    private String endTime;
    private String status;
    private String createTime;
}