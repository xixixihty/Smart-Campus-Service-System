package com.hxq.smart_campus.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingAnalysisVO {
    private Integer totalBorrows;
    private Integer monthBorrows;
    private String preference;
    private String favCategory;
    private String analysis;
}