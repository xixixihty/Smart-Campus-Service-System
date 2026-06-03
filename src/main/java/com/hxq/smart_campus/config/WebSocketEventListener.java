package com.hxq.smart_campus.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.debug("WebSocket连接建立: sessionId={}", accessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        String memberKey = (String) redisTemplate.opsForValue().get("ws:session:" + sessionId);
        if (memberKey != null) {
            redisTemplate.opsForSet().remove("ws:online:users", memberKey);
            redisTemplate.delete("ws:session:" + sessionId);
            log.debug("WebSocket断开连接: sessionId={}, memberKey={}", sessionId, memberKey);
        }
    }
}