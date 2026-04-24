package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class LoginResponseDTO {
    private String token; // 登录令牌
    private String userType; // 用户类型
    private Long userId; // 用户ID
    private String username; // 用户名（学号或工号）
    private String name; // 姓名
    private String status; // 状态
    private String accountStatus; // 账号状态
    private LocalDateTime expireTime; // 令牌过期时间

}
