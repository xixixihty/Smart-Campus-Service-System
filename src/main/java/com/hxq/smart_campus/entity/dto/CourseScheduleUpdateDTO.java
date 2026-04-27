package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class CourseScheduleUpdateDTO {
    @NotNull(message = "排课ID不能为空")
    private Long id;                // 排课ID，必填
    
    private Long teacherId;         // 授课教师ID，可选
    
    private Long classroomId;       // 教室ID，可选
    
    private List<Long> classIds;    // 授课班级ID列表，可选
    
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    private Integer weekDay;        // 星期几，可选
    
    @Min(value = 1, message = "开始节次必须大于0")
    private Integer startSection;   // 开始节次，可选
    
    @Min(value = 1, message = "结束节次必须大于0")
    private Integer endSection;     // 结束节次，可选
    
    private String weekRange;       // 周次范围，可选
}
