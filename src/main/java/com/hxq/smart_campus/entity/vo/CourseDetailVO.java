package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDetailVO {
    private Long id;                // 课程ID
    private String courseCode;      // 课程代码
    private String courseName;      // 课程名称
    private BigDecimal credit;      // 学分
    private Integer hours;          // 总学时
    private String type;            // 课程类型
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
