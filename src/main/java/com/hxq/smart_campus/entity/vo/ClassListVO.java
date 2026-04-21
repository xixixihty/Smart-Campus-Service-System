package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class ClassListVO {
    private Long id;                // 班级ID
    private String majorName;       // 专业名称
    private String className;       // 班级名称
    private String headTeacherName; // 班主任姓名
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
}
