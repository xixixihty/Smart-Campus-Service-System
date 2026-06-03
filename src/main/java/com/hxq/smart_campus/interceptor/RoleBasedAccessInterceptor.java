package com.hxq.smart_campus.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleBasedAccessInterceptor implements HandlerInterceptor {

    private static final Map<String, Set<String>> ROLE_MAPPING = Map.of(
            "/api/admin", Set.of(USER_TYPE_ADMIN),
            "/api/teacher", Set.of(USER_TYPE_TEACHER),
            "/api/user", Set.of(USER_TYPE_STUDENT, USER_TYPE_TEACHER),
            "/api/ai/admin", Set.of(USER_TYPE_ADMIN),
            "/api/ai/teacher", Set.of(USER_TYPE_TEACHER),
            "/api/ai/user", Set.of(USER_TYPE_STUDENT, USER_TYPE_TEACHER)
    );

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 响应已提交（如SSE流式响应进行中），跳过权限校验，避免 getWriter() 冲突
        if (response.isCommitted()) {
            return true;
        }

        String path = request.getServletPath();
        String userType = SecurityUtils.getCurrentUserType();

        if (userType == null || userType.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error("401", "未登录或登录已过期")));
            return false;
        }

        for (Map.Entry<String, Set<String>> entry : ROLE_MAPPING.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                if (!entry.getValue().contains(userType)) {
                    log.warn("角色越权访问: userType={}, path={}, allowedRoles={}", userType, path, entry.getValue());
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(Result.error("403", "无权访问该资源")));
                    return false;
                }
                return true;
            }
        }
        return true;
    }
}