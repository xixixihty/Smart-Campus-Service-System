package com.hxq.smart_campus.entity.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 成绩审计日志实体
 */
@Data
public class ScoreAuditLog {
    private Long id;
    private Long scoreEntryId;
    private Long operatorId;
    private String operatorType;
    private String operation;
    private String beforeData;
    private String afterData;
    private String ipAddress;
    private LocalDateTime createTime;
}