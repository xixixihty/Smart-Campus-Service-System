package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class StudentUpdateDTO {
    private Long id;                // 学生ID，必填
    private String name;            // 姓名，可选
    private String gender;          // 性别，可选
    private Long classId;           // 所属班级ID，可选
    private String status;          // 学籍状态，可选
    private String idCard;          // 身份证号，可选
    private String phone;           // 手机号，可选
    private String accountStatus;   // 账号状态，可选
}
