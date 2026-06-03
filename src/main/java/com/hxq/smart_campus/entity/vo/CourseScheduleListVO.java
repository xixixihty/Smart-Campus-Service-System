package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class CourseScheduleListVO {
    private Long id;                // 排课ID
    private String semesterName;    // 学期名称
    private String courseName;      // 课程名称
    private String teacherName;     // 教师姓名
    private String classroomName;   // 教室名称（教学楼+教室门牌号）
    private String classNames;      // 授课班级名称（逗号分隔）
    private Integer weekDay;        // 星期几
    private Integer startSection;   // 开始节次
    private Integer endSection;     // 结束节次
    private String weekRange;       // 周次范围
}
