package com.hxq.smart_campus.config;

import com.hxq.smart_campus.entity.info.LoginUserInfo;
import com.hxq.smart_campus.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders == null || authHeaders.isEmpty()) {
                throw new MessageDeliveryException("缺少Authorization头");
            }

            String authHeader = authHeaders.get(0);
            if (!authHeader.startsWith("Bearer ")) {
                throw new MessageDeliveryException("Authorization格式错误");
            }

            String token = authHeader.substring(7);
            if (!JwtUtils.validateToken(token)) {
                throw new MessageDeliveryException("Token无效或已过期");
            }

            Claims claims = JwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String userType = claims.get("userType", String.class);
            String name = claims.get("name", String.class);

            String sessionId = accessor.getSessionId();
            String memberKey = userId + ":" + userType;

            redisTemplate.opsForValue().set("ws:session:" + sessionId, memberKey, 24, TimeUnit.HOURS);
            redisTemplate.opsForSet().add("ws:online:users", memberKey);

            accessor.setUser(new WsPrincipal(userId, userType, name));
            log.debug("WebSocket认证成功，用户: {}", memberKey);
        }
        return message;
    }

    public static class WsPrincipal implements Principal {
        private final Long userId;
        private final String userType;
        private final String displayName;

        public WsPrincipal(Long userId, String userType, String displayName) {
            this.userId = userId;
            this.userType = userType;
            this.displayName = displayName;
        }

        @Override
        public String getName() {
            return String.valueOf(userId);
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserType() {
            return userType;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}