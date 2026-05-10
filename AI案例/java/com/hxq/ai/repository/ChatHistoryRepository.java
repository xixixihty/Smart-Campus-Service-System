package com.hxq.ai.repository;


import java.util.List;

public interface ChatHistoryRepository {

    /**
     * 保存聊天记录
     * @param chatId 聊天记录ID
     */
    void save(String type, String chatId);
    /**
     * 根据类型查询聊天记录
     * @param type 类型
     * @return 会话ID列表
     */
    List<String> getChatIds(String type);
}
