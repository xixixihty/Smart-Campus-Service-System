package com.hxq.smart_campus.controller.common;

import com.hxq.smart_campus.context.SecurityContext;
import com.hxq.smart_campus.entity.dto.LoginDTO;
import com.hxq.smart_campus.entity.dto.LoginResponseDTO;
import com.hxq.smart_campus.entity.info.LoginUserInfo;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * - 用户名 ：T2021001（教师）
 * - 密码 ：Password123!
 */


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户登录接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @GetMapping("/validate")
    @Operation(summary = "验证token是否有效")
    public Result<Map<String, Object>> validateToken() {
        LoginUserInfo userInfo = SecurityContext.getUserInfo();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userInfo.getUserId());
        data.put("username", userInfo.getUsername());
        data.put("name", userInfo.getName());
        data.put("userType", userInfo.getUserType());
        return Result.success(data);
    }
}
