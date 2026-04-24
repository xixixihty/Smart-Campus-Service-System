package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentCreateDTO {
    private String studentNo;       // 学号，必填
    private String name;            // 姓名，必填
    private String gender;          // 性别，必填
    private Long classId;           // 所属班级ID，必填
    private LocalDate enrollmentDate; // 入学日期，必填
    private String idCard;          // 身份证号，可选
    private String phone;           // 手机号，可选
    private String password;        // 密码，必填
}
