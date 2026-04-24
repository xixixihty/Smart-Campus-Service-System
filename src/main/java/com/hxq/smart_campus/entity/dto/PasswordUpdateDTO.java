package com.hxq.smart_campus.entity.dto;

import lombok.Data;

/**
 * 密码更新DTO
 *
 * @author XiongQi He
 * @since 2026-04-22
 */
@Data
public class PasswordUpdateDTO {
    private Long id;             // 用户ID
    private String oldPassword;  // 旧密码
    private String newPassword;  // 新密码
    private String confirmPassword; // 确认新密码

    
}
