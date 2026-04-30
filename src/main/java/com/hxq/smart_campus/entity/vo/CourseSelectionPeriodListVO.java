package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class CourseSelectionPeriodListVO {
    private Long id;                // 时间段ID
    private String semesterName;    // 学期名称
    private String startTime;       // 选课开始时间（格式化显示）
    private String endTime;         // 选课结束时间（格式化显示）
    private String createTime;      // 创建时间（格式化显示）
}
