package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class TeacherCreateDTO {
    private String teacherNo;       // 工号，必填
    private String name;            // 姓名，必填
    private String gender;          // 性别，必填
    private Long collegeId;         // 所属学院ID，必填
    private String title;           // 职称，可选
    private String phone;           // 手机号，可选
    private String email;           // 邮箱，可选
    private String password;        // 密码，必填
}
