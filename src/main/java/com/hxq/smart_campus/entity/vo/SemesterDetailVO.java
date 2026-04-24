package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterDetailVO {
    private Long id;                // 学期ID
    private String name;            // 学期名称
    private LocalDate startDate;    // 开学日期
    private LocalDate endDate;      // 结束日期
    private Boolean isCurrent;      // 是否当前学期
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
