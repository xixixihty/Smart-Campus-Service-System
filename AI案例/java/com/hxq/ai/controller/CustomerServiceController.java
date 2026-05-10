package com.hxq.ai.controller;


import com.hxq.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class CustomerServiceController {
    private final ChatClient serviceChatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/service", method = RequestMethod.POST, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> service(@RequestParam String prompt, @RequestParam(required = false) String chatId) {
        System.out.println("收到客服模块请求: prompt=" + prompt + ", chatId=" + chatId);
        // 保存会话ID
        chatHistoryRepository.save("service", chatId);
        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()// 流式调用
                .content()
                .doOnNext(content -> System.out.println("客服模块收到响应: " + content))
                .onErrorResume(e -> {
                    System.err.println("客服模块AI调用失败: " + e.getMessage());
                    e.printStackTrace();
                    return Flux.just("{\"error\": \"AI服务暂时不可用，请稍后重试。错误信息: " + e.getMessage() + "\"}");
                });
    }
}
