package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username; // 用户名（学号或工号）
    private String password; // 密码
    private String userType; // 用户类型：student或teacher


}
