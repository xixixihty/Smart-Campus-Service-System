package com.hxq.smart_campus.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRecommendVO {
    private Long id;
    private String bookName;
    private String author;
    private String reason;
    private Integer rating;
}