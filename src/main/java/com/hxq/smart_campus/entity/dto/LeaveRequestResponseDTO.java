package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveRequestResponseDTO {
    private Long id;                // 请假申请ID
    private Long studentId;         // 学生ID
    private String studentName;     // 学生姓名
    private String leaveType;       // 请假类型
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime;   // 结束时间
    private String reason;          // 请假事由
    private String status;          // 状态
    private LocalDateTime createTime; // 申请时间
    private LocalDateTime updateTime; // 最后修改时间
}
