package com.hxq.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class CourseQuery {
    @ToolParam(required = false, description = "课程方向")
    private String direction;
    @ToolParam(required = false, description = "目标薪资")
    private String targetSalary;
}
