package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SemesterResponseDTO {
    private Long id;                // 学期ID
    private String name;            // 学期名称
    private LocalDate startDate;    // 开学日期
    private LocalDate endDate;      // 结束日期
    private Boolean isCurrent;      // 是否当前学期
    private String status;          // 状态
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
