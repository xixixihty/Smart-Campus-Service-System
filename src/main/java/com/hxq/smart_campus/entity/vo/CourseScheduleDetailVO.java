package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class CourseScheduleDetailVO {
    private Long id;                // 排课ID
    private Long semesterId;        // 学期ID
    private String semesterName;    // 学期名称
    private Long courseId;          // 课程ID
    private String courseName;      // 课程名称
    private Long teacherId;         // 授课教师ID
    private String teacherName;     // 教师姓名
    private Long classroomId;       // 教室ID
    private String classroomName;   // 教室名称（教学楼+教室门牌号）
    private List<Long> classIds;    // 授课班级ID列表
    private List<String> classNames; // 授课班级名称列表
    private Integer weekDay;        // 星期几
    private Integer startSection;   // 开始节次
    private Integer endSection;     // 结束节次
    private String weekRange;       // 周次范围
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
