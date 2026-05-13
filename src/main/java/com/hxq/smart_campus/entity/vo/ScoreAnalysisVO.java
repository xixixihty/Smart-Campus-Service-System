package com.hxq.smart_campus.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreAnalysisVO {
    private String avgScore;
    private String maxScore;
    private String minScore;
    private String gpa;
    private String suggestion;
}