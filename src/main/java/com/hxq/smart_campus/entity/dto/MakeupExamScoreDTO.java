package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MakeupExamScoreDTO {
    private Long id;                // 补考ID，必填
    private BigDecimal makeupScore; // 补考成绩，必填
}
