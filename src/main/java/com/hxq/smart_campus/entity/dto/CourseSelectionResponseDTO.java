package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseSelectionResponseDTO {
    private Long id;                // 选课ID
    private Long studentId;         // 学生ID
    private String studentName;     // 学生姓名
    private Long courseId;          // 课程ID
    private String courseName;      // 课程名称
    private BigDecimal credit;      // 学分
    private Long semesterId;        // 学期ID
    private String semesterName;    // 学期名称
    private String status;          // 状态
    private BigDecimal score;       // 最终成绩
    private BigDecimal scorePoint;  // 绩点
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
