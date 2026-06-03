package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class MyLeaveRequestVO {
    private Long id;
    private String applicantType;
    private String applicantName;
    private String approverName;
    private String leaveType;
    private String startTime;
    private String endTime;
    private String reason;
    private String status;
    private Integer isCourseRescheduled;
    private String createTime;
}