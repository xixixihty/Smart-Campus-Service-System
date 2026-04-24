package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.dto.LoginDTO;
import com.hxq.smart_campus.entity.dto.LoginResponseDTO;

public interface AuthService {
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录响应
     */
    LoginResponseDTO login(LoginDTO loginDTO);
}
