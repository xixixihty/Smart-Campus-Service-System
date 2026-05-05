package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LeaveRequestDetailVO {
    private Long id;                // 请假申请ID
    private Long studentId;         // 学生ID
    private String studentName;     // 学生姓名
    private String leaveType;       // 请假类型
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime;   // 结束时间
    private String reason;          // 请假事由
    private String status;          // 状态
    private String createTime;      // 申请时间（格式化显示）
    private String updateTime;      // 最后修改时间
    private List<LeaveApprovalLogVO> approvalLogs; // 审批记录
}
