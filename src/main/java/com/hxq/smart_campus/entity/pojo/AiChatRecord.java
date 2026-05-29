package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatRecord {
    private Long id;
    private Long userId;
    private String sessionId;
    private String role;
    private String content;
    private Integer tokenCount;
    private LocalDateTime createTime;
}