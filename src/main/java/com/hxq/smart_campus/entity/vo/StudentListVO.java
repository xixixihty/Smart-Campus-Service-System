package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class StudentListVO {
    private Long id;                // 学生ID
    private String studentNo;       // 学号
    private String name;            // 姓名
    private String gender;          // 性别
    private String className;       // 班级名称
    private String status;          // 学籍状态
    private String accountStatus;   // 账号状态
    private String createTime;      // 创建时间（格式化显示）
}
