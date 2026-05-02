package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreEntryListVO {
    private Long id;                // 成绩ID
    private String courseName;      // 课程名称
    private String studentName;     // 学生姓名
    private String studentNo;       // 学号
    private String semesterName;    // 学期名称
    private BigDecimal totalScore;  // 总评成绩
    private BigDecimal scorePoint;  // 绩点
    private String examStatus;      // 考试状态
    private String createTime;      // 创建时间（格式化显示）
}
