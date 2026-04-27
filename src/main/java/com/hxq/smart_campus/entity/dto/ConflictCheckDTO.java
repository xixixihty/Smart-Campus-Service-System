package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class ConflictCheckDTO {
    @NotNull(message = "学期ID不能为空")
    private Long semesterId;        // 学期ID，必填
    
    @NotNull(message = "授课教师ID不能为空")
    private Long teacherId;         // 授课教师ID，必填
    
    @NotNull(message = "教室ID不能为空")
    private Long classroomId;       // 教室ID，必填
    
    @NotEmpty(message = "授课班级ID列表不能为空")
    private List<Long> classIds;    // 授课班级ID列表，必填
    
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    private Integer weekDay;        // 星期几，必填
    
    @NotNull(message = "开始节次不能为空")
    @Min(value = 1, message = "开始节次必须大于0")
    private Integer startSection;   // 开始节次，必填
    
    @NotNull(message = "结束节次不能为空")
    @Min(value = 1, message = "结束节次必须大于0")
    private Integer endSection;     // 结束节次，必填
    
    @NotBlank(message = "周次范围不能为空")
    private String weekRange;       // 周次范围，必填
}
