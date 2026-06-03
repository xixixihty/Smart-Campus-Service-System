package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseSelectionListVO {
    private Long id;                // 选课ID
    private Long courseId;          // 课程ID
    private Long studentId;         // 学生ID
    private String studentName;     // 学生姓名
    private String courseName;      // 课程名称
    private BigDecimal credit;      // 学分
    private String semesterName;    // 学期名称
    private Long semesterId;        // 学期ID
    private String status;          // 状态
    private BigDecimal score;       // 最终成绩
    private BigDecimal scorePoint;  // 绩点
    private String createTime;      // 创建时间
    private String updateTime;      // 更新时间
}
