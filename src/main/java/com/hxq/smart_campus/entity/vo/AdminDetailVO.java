package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class AdminDetailVO {
    private Long id;
    private String adminNo;
    private String name;
    private String gender;
    private String role;
    private Long collegeId;
    private String collegeName;
    private String phone;
    private String email;
    private String accountStatus;
    private String password;
    private String createTime;
    private String updateTime;
}