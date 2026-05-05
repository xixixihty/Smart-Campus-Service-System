package com.hxq.smart_campus.entity.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class LeaveApprovalLog {
    private Long id;
    private Long leaveRequestId;
    private Long approverId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
    private String result;
    private String comment;
}
