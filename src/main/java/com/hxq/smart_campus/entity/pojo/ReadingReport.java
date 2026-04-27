package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingReport {
    private Long id;
    private Long userId;
    private String semester;
    private Integer totalBorrow;
    private String favCategory;
    private String analysisText;
    private LocalDateTime createTime;
}
