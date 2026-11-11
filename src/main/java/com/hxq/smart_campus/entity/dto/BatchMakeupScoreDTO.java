package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量补考成绩录入请求
 */
@Data
public class BatchMakeupScoreDTO {
    private Long sessionId;
    private List<ScoreItem> scores;

    @Data
    public static class ScoreItem {
        private Long studentId;
        private BigDecimal score;
    }
}