package com.hxq.ai.controller;

import com.hxq.ai.entity.vo.MessageVO;
import com.hxq.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;



    @GetMapping("/{type}")
    public List<String> getChatIds(@PathVariable String type) {
        return chatHistoryRepository.getChatIds(type);
    }

    @GetMapping("/{type}/{chatId}")
    public List<MessageVO> getChatHistory(@PathVariable String type, @PathVariable String chatId) {
        List<Message> messages = chatMemory.get(chatId);  // 仅需一个参数
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        return messages.stream().map(MessageVO::new).toList();
    }
}
