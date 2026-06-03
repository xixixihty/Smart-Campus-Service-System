package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class CourseRescheduleVO {
    private Long id;
    private Long leaveRequestId;
    private Long courseScheduleId;
    private Long teacherId;
    private String courseName;
    private String className;
    private String teacherName;
    private Integer originalWeekDay;
    private Integer originalStartSection;
    private Integer originalEndSection;
    private String originalWeekRange;
    private Long originalClassroomId;
    private String originalClassroomName;
    private Integer newWeekDay;
    private Integer newStartSection;
    private Integer newEndSection;
    private String newWeekRange;
    private Long newClassroomId;
    private String newClassroomName;
    private String status;
    private String reason;
    private String createTime;
    private String updateTime;
}