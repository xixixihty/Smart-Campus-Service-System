package com.hxq.smart_campus.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * JWT工具类，用于生成、解析和验证JWT token
 */

@Slf4j
@Component
public class JwtUtils {

    private static String secretStatic;
    private static long expirationTimeStatic;

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

    @PostConstruct
    public void init() {
        if (this.secret == null || this.secret.isBlank()
                || this.secret.contains("must-be-at-least")) {
            throw new IllegalStateException(
                    "JWT密钥未配置或使用了不安全默认值，请在application.yaml中配置 jwt.secret");
        }
        if (this.secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT密钥长度不足32字节，请使用至少256位的密钥");
        }
        secretStatic = this.secret;
        expirationTimeStatic = this.expirationTime;
    }

    private static final String USER_ID_CLAIM = "userId";
    private static final String USERNAME_CLAIM = "username";
    private static final String NAME_CLAIM = "name";
    private static final String USER_TYPE_CLAIM = "userType";

    private JwtUtils() {}

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretStatic.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId, String username, String name, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userId);
        claims.put(USERNAME_CLAIM, username);
        claims.put(NAME_CLAIM, name);
        claims.put(USER_TYPE_CLAIM, userType);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTimeStatic);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token 已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("JWT token 解析失败: {}", e.getMessage());
            throw e;
        }
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(USER_ID_CLAIM, Long.class);
    }

    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(USERNAME_CLAIM, String.class);
    }

    public static String getNameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(NAME_CLAIM, String.class);
    }

    public static String getUserTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(USER_TYPE_CLAIM, String.class);
    }
}