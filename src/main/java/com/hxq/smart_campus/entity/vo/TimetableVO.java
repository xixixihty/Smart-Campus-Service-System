package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class TimetableVO {
    private Long courseScheduleId;  // 排课ID
    private String courseName;      // 课程名称
    private String teacherName;     // 教师姓名
    private String classroomName;   // 教室名称（教学楼+教室门牌号）
    private String classIds;        // 授课班级ID列表（JSON数组）
    private String classNames;      // 授课班级名称列表
    private Integer weekDay;        // 星期几
    private Integer startSection;   // 开始节次
    private Integer endSection;     // 结束节次
    private String weekRange;       // 周次范围
}
