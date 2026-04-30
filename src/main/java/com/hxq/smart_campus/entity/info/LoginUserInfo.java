package com.hxq.smart_campus.entity.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserInfo {
    private Long userId;
    private String username;
    private String name;
    private String userType;
}