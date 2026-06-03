package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Admin {
    private Long id;
    private String adminNo;
    private String name;
    private String gender;
    private String role;
    private Long collegeId;
    private String phone;
    private String email;
    private String accountStatus;
    private String password;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}