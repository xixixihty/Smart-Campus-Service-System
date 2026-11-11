package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单个学生补考成绩录入请求
 */
@Data
public class MakeupScoreInputDTO {
    private Long sessionId;
    private Long studentId;
    private BigDecimal score;
}