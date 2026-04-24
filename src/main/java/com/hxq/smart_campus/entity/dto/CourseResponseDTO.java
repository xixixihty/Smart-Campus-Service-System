package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseResponseDTO {
    private Long id;                // 课程ID
    private String courseCode;      // 课程代码
    private String courseName;      // 课程名称
    private BigDecimal credit;      // 学分
    private Integer hours;          // 总学时
    private String type;            // 课程类型
    private String status;          // 状态
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
