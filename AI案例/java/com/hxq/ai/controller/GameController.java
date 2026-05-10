package com.hxq.ai.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/ai")

public class GameController {
    private final ChatClient gameChatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameController(@Qualifier("gameChatClient") ChatClient gameChatClient) {
        this.gameChatClient = gameChatClient;
    }

    @RequestMapping(value = "/game", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String chat(@RequestParam String prompt, @RequestParam(required = false) String chatId) {
        System.out.println("收到游戏模块请求: prompt=" + prompt + ", chatId=" + chatId);
        try {
            String response = gameChatClient.prompt()
                    .user(prompt)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()// 非流式调用
                    .content();
            System.out.println("游戏模块收到响应: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("游戏模块AI调用失败: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"AI服务暂时不可用，请稍后重试。错误信息: " + e.getMessage() + "\"}";
        }
    }
}
