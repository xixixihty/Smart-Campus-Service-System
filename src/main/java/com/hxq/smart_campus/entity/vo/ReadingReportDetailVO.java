package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class ReadingReportDetailVO {
    private Long id;                // 报告ID
    private Long userId;            // 用户ID
    private String userName;        // 用户姓名
    private String semester;        // 学期
    private Integer totalBorrow;    // 总借阅量
    private String favCategory;     // 偏好分类
    private String analysisText;    // AI生成的分析文本
    private String createTime;      // 生成时间（格式化显示）
}
