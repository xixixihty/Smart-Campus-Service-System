package com.hxq.smart_campus.entity.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TeacherResponseDTO {
    private Long id;                // 教师ID
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
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
