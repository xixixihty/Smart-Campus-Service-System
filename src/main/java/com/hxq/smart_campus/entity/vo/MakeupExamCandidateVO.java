package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 待补考学生信息（资格扫描结果）
 */
@Data
public class MakeupExamCandidateVO {
    private Long scoreEntryId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private BigDecimal totalScore;
    private String examStatus;
    private String reason;
}