package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.dto.AiChatHistoryDTO;
import com.hxq.smart_campus.entity.dto.AiSessionDTO;

import java.util.List;

public interface AiChatRecordService {

    void saveMessagesAsync(Long userId, String sessionId, String userMessage, String assistantMessage);

    List<AiSessionDTO> listSessions(Long userId);

    List<AiChatHistoryDTO> getSessionHistory(String sessionId, Long userId, int limit);

    boolean deleteSession(String sessionId, Long userId);
}