package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

@Data
public class CourseSchedule {
    private Long id;
    private Integer semesterId;
    private Long courseId;
    private Long teacherId;
    private Long classroomId;
    private String classIds;
    private Integer weekDay;
    private Byte startSection;
    private Byte endSection;
    private String weekRange;
}
