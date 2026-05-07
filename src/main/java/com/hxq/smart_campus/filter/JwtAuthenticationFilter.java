package com.hxq.smart_campus.filter;

import com.hxq.smart_campus.context.SecurityContext;
import com.hxq.smart_campus.entity.info.LoginUserInfo;
import com.hxq.smart_campus.utils.JwtUtils;
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
        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                if (JwtUtils.validateToken(token)) {
                    Claims claims = JwtUtils.parseToken(token);
                    LoginUserInfo userInfo = new LoginUserInfo(
                            claims.get("userId", Long.class),
                            claims.get("username", String.class),
                            claims.get("name", String.class),
                            claims.get("userType", String.class)
                    );
                    SecurityContext.setUserInfo(userInfo);
                    log.debug("JWT认证成功，用户: {}", userInfo.getUsername());
                }
            }
        } catch (JwtException e) {
            response.sendError(401, "JWT认证失败");
            return;
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                SecurityContext.clear();
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") || path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") || path.startsWith("/doc.html");
    }
}