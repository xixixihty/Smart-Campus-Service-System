package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

@Data
public class CourseSchedule {
    private Long id;
    private Long semesterId;
    private Long courseId;
    private Long teacherId;
    private Long classroomId;
    private Integer weekDay;
    private Integer startSection;
    private Integer endSection;
    private String weekRange;
}
