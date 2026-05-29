package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MyCourseSelectionVO {
    private Long id;                // 选课ID
    private Long courseId;          // 课程ID
    private String courseName;      // 课程名称
    private BigDecimal credit;      // 学分
    private String semesterName;    // 学期名称
    private String status;          // 状态
    private BigDecimal score;       // 最终成绩
    private BigDecimal scorePoint;  // 绩点
}
