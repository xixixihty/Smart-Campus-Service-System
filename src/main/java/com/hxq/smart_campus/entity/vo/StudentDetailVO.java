package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentDetailVO {
    private Long id;                // 学生ID
    private String studentNo;       // 学号
    private String name;            // 姓名
    private String gender;          // 性别
    private Long classId;           // 所属班级ID
    private String className;       // 班级名称
    private LocalDate enrollmentDate; // 入学日期
    private String status;          // 学籍状态
    private String idCard;          // 身份证号
    private String phone;           // 手机号
    private String accountStatus;   // 账号状态
    private String password;        // 密码（仅用于登录认证）
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
