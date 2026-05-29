package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatSession {
    private Long id;
    private Long userId;
    private String sessionId;
    private String title;
    private Integer messageCount;
    private LocalDateTime lastMessageAt;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}