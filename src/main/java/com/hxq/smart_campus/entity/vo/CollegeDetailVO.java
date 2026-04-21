package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class CollegeDetailVO {
    private Long id;                // 学院ID
    private String collegeCode;     // 学院代码
    private String collegeName;     // 学院名称
    private String dean;            // 院长
    private String contactPhone;    // 联系电话
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
