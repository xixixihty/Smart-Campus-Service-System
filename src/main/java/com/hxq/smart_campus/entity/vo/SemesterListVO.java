package com.hxq.smart_campus.entity.vo;

import lombok.Data;
@Data
public class SemesterListVO {
    private Long id;                // 学期ID
    private String name;            // 学期名称
    private String startDate;       // 开学日期（格式化显示）
    private String endDate;         // 结束日期（格式化显示）
    private Boolean isCurrent;      // 是否当前学期
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
}
