package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GpaVO {
    private Long studentId;         // 学生ID
    private String studentNo;       // 学号
    private String studentName;     // 学生姓名
    private BigDecimal gpa;         // 平均绩点
    private Integer courseCount;    // 课程数量
    private BigDecimal totalCredits; // 总学分
    private BigDecimal earnedCredits; // 已获得学分
    private Long semesterId;        // 学期ID（仅按学期查询时有值）
    private String semesterName;    // 学期名称（仅按学期查询时有值）
}
