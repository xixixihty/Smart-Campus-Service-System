package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreDistributionItem {
    private String scoreRange;
    private Integer count;
    private BigDecimal percentage;
}
