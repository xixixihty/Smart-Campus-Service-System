package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseReschedule {
    private Long id;
    private Long leaveRequestId;
    private Long courseScheduleId;
    private Long teacherId;
    private Integer originalWeekDay;
    private Integer originalStartSection;
    private Integer originalEndSection;
    private String originalWeekRange;
    private Long originalClassroomId;
    private Integer newWeekDay;
    private Integer newStartSection;
    private Integer newEndSection;
    private String newWeekRange;
    private Long newClassroomId;
    private String status;
    private String reason;
    private Long approverId;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}