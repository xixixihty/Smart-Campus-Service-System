package com.hxq.smart_campus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxq.smart_campus.entity.dto.AiChatHistoryDTO;
import com.hxq.smart_campus.entity.dto.AiSessionDTO;
import com.hxq.smart_campus.service.AiChatRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class AiChatRecordServiceImpl implements AiChatRecordService {

    private static final String SESSION_LIST_KEY = "ai:session:%s:list";
    private static final String SESSION_INFO_KEY = "ai:session:%s:info";
    private static final String CHAT_KEY = "ai:chat:%s";
    private static final Duration TTL = Duration.ofDays(7);
    private static final int MAX_MESSAGES = 200;
    private static final int MAX_SESSIONS = 50;
    private static final String LUA_SAVE_CHAT_PATH = "lua/save_chat.lua";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public AiChatRecordServiceImpl(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveMessagesAsync(Long userId, String sessionId, String userMessage, String assistantMessage) {
        saveMessages(userId, sessionId, userMessage, assistantMessage);
    }

    private void saveMessages(Long userId, String sessionId, String userMessage, String assistantMessage) {
        try {
            long now = System.currentTimeMillis();
            String sessionListKey = String.format(SESSION_LIST_KEY, userId);
            String sessionInfoKey = String.format(SESSION_INFO_KEY, sessionId);
            String chatKey = String.format(CHAT_KEY, sessionId);

            String userMsgJson = buildMessageJson("user", userMessage, now);
            String assistantMsgJson = buildMessageJson("assistant", assistantMessage, now);
            String title = buildTitle(userMessage);

            String script = loadScript(LUA_SAVE_CHAT_PATH);
            List<String> keys = Arrays.asList(sessionListKey, sessionInfoKey, chatKey);
            Object[] args = {
                    sessionId,
                    String.valueOf(now),
                    userMsgJson,
                    assistantMsgJson,
                    title,
                    String.valueOf(userId),
                    String.valueOf(TTL.getSeconds()),
                    String.valueOf(MAX_MESSAGES),
                    String.valueOf(MAX_SESSIONS)
            };

            stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), keys, args);

            log.info("AI对话记录已保存到Redis: sessionId={}, userId={}", sessionId, userId);
        } catch (JsonProcessingException e) {
            log.error("AI对话记录JSON序列化失败: sessionId={}, userId={}", sessionId, userId, e);
        } catch (Exception e) {
            log.error("AI对话记录保存到Redis失败: sessionId={}, userId={}", sessionId, userId, e);
        }
    }

    @Override
    public List<AiSessionDTO> listSessions(Long userId) {
        String sessionListKey = String.format(SESSION_LIST_KEY, userId);
        Set<String> sessionIds = stringRedisTemplate.opsForZSet()
                .reverseRange(sessionListKey, 0, MAX_SESSIONS - 1);

        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<AiSessionDTO> sessions = new ArrayList<>();
        for (String sessionId : sessionIds) {
            String infoKey = String.format(SESSION_INFO_KEY, sessionId);
            Map<Object, Object> info = stringRedisTemplate.opsForHash().entries(infoKey);
            if (info.isEmpty()) {
                continue;
            }

            AiSessionDTO dto = new AiSessionDTO();
            dto.setSessionId(sessionId);
            dto.setTitle(getString(info, "title"));
            dto.setMessageCount(getInt(info, "messageCount"));
            dto.setLastMessageAt(parseDateTime(getString(info, "lastMessageAt")));
            dto.setCreateTime(parseDateTime(getString(info, "createTime")));
            sessions.add(dto);
        }
        return sessions;
    }

    @Override
    public List<AiChatHistoryDTO> getSessionHistory(String sessionId, int limit) {
        String chatKey = String.format(CHAT_KEY, sessionId);
        List<String> messages = stringRedisTemplate.opsForList().range(chatKey, 0, limit - 1);

        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        List<AiChatHistoryDTO> history = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> msg = objectMapper.readValue(messages.get(i), Map.class);
                AiChatHistoryDTO dto = new AiChatHistoryDTO();
                dto.setId((long) i);
                dto.setSessionId(sessionId);
                dto.setRole(getString(msg, "role"));
                dto.setContent(getString(msg, "content"));
                dto.setCreateTime(parseDateTime(getString(msg, "time")));
                history.add(dto);
            } catch (JsonProcessingException e) {
                log.warn("解析聊天消息JSON失败: sessionId={}, index={}", sessionId, i);
            }
        }
        return history;
    }

    @Override
    public boolean deleteSession(String sessionId, Long userId) {
        String sessionListKey = String.format(SESSION_LIST_KEY, userId);
        String sessionInfoKey = String.format(SESSION_INFO_KEY, sessionId);
        String chatKey = String.format(CHAT_KEY, sessionId);

        stringRedisTemplate.opsForZSet().remove(sessionListKey, sessionId);
        stringRedisTemplate.delete(Arrays.asList(sessionInfoKey, chatKey));

        log.info("AI会话已从Redis删除: sessionId={}, userId={}", sessionId, userId);
        return true;
    }

    private String buildMessageJson(String role, String content, long timestamp) throws JsonProcessingException {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("role", role);
        msg.put("content", content != null ? content : "");
        msg.put("time", timestamp);
        return objectMapper.writeValueAsString(msg);
    }

    private String buildTitle(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return "新对话";
        }
        return userMessage.length() > 50 ? userMessage.substring(0, 50) : userMessage;
    }

    private String loadScript(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("无法加载脚本文件: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取脚本文件失败: " + path, e);
        }
    }

    private String getString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getInt(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDateTime parseDateTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return null;
        try {
            long millis = Long.parseLong(timestamp);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}