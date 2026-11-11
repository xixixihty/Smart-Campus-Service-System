package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补考学生关联实体（场次与学生多对多）
 */
@Data
public class MakeupExamStudent {
    private Long id;
    private Long sessionId;
    private Long studentId;
    private Long scoreEntryId;
    private BigDecimal makeupScore;
    private String studentStatus;
    private LocalDateTime notificationTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}