package com.hxq.smart_campus.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowStatisticsDTO {
    private Integer totalBorrows;
    private Integer currentBorrows;
    private Integer overdueBorrows;
    private Double averageBorrowDays;
    private List<CategoryBorrowDTO> categoryBorrows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBorrowDTO {
        private String categoryName;
        private Integer borrowCount;
    }
}