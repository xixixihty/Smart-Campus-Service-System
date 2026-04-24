package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class TeacherUpdateDTO {
    private Long id;                // 教师ID，必填
    private String name;            // 姓名，可选
    private String gender;          // 性别，可选
    private Long collegeId;         // 所属学院ID，可选
    private String title;           // 职称，可选
    private String phone;           // 手机号，可选
    private String email;           // 邮箱，可选
    private String status;          // 状态，可选
    private String accountStatus;   // 账号状态，可选
}
