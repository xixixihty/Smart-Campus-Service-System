package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class MajorDetailVO {
    private Long id;                // 专业ID
    private Long collegeId;         // 所属学院ID
    private String collegeName;     // 学院名称
    private String majorCode;       // 专业代码
    private String majorName;       // 专业名称
    private Integer studyYears;     // 学制年限
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
