package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingReportResponseDTO {
    private Long id;                // 报告ID
    private Long userId;            // 用户ID
    private String userName;        // 用户姓名
    private String semester;        // 学期
    private Integer totalBorrow;    // 总借阅量
    private String favCategory;     // 偏好分类
    private String analysisText;    // AI生成的分析文本
    private LocalDateTime createTime; // 生成时间
}
