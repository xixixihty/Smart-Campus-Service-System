package com.hxq.smart_campus.entity.vo;

import lombok.Data;


@Data
public class TeacherDetailVO {
    private Long teacherId;         // 教师ID
    private String teacherNo;       // 工号
    private String name;            // 姓名
    private String gender;          // 性别
    private Long collegeId;         // 所属学院ID
    private String collegeName;     // 学院名称
    private String title;           // 职称
    private String phone;           // 手机号
    private String email;           // 邮箱
    private String status;          // 状态
    private String accountStatus;   // 账号状态
    private String password;        // 密码（仅用于登录认证）
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
