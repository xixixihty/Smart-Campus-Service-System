package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class MyLeaveRequestVO {
    private Long id;                // 请假申请ID
    private String leaveType;       // 请假类型
    private String startTime;       // 开始时间（格式化显示）
    private String endTime;         // 结束时间（格式化显示）
    private String reason;          // 请假事由
    private String status;          // 状态
    private String createTime;      // 申请时间（格式化显示）
}
