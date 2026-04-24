package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class TeacherListVO {
    private Long id;                // 教师ID
    private String teacherNo;       // 工号
    private String name;            // 姓名
    private String gender;          // 性别
    private String collegeName;     // 学院名称
    private String title;           // 职称
    private String status;          // 状态
    private String accountStatus;   // 账号状态
    private String createTime;      // 创建时间（格式化显示）
}
