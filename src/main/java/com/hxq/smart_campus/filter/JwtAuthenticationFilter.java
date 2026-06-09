package com.hxq.smart_campus.filter;

import com.hxq.smart_campus.context.SecurityContext;
import com.hxq.smart_campus.entity.info.LoginUserInfo;
import com.hxq.smart_campus.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                if (!JwtUtils.validateToken(token)) {
                    log.warn("JWT token无效，拒绝访问: {}", request.getServletPath());
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":\"401\",\"msg\":\"登录已过期，请重新登录\"}");
                    return;
                }
                Claims claims = JwtUtils.parseToken(token);
                LoginUserInfo userInfo = new LoginUserInfo(
                        claims.get("userId", Long.class),
                        claims.get("username", String.class),
                        claims.get("name", String.class),
                        claims.get("userType", String.class)
                );
                SecurityContext.setUserInfo(userInfo);
                log.debug("JWT认证成功，用户: {}", userInfo.getUsername());
            } else {
                log.warn("缺少Authorization请求头，拒绝访问: {}", request.getServletPath());
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":\"401\",\"msg\":\"请先登录\"}");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("JWT认证失败: {}", e.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"登录已过期，请重新登录\"}");
        } finally {
            SecurityContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/login")
               || path.startsWith("/swagger-ui")
               || path.startsWith("/v3/api-docs")
               || path.startsWith("/doc.html")
               || path.startsWith("/ws")
               || path.startsWith("/api/ws");
    }
}