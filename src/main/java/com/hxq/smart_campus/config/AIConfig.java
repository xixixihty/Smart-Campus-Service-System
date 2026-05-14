package com.hxq.smart_campus.config;

import com.hxq.smart_campus.model.AlibabaOpenAiChatModel;
import com.hxq.smart_campus.service.ai.AdminAITools;
import com.hxq.smart_campus.service.ai.UserAITools;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AIConfig {

    @Bean
    @Primary
    public ChatModel alibabaChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("qwen-max")
                .temperature(0.7)
                .build();
        return new AlibabaOpenAiChatModel(openAiApi, options,
                ToolCallingManager.builder().build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.NOOP);
    }

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一个智慧校园系统的AI助手，需要基于提供的数据进行专业分析，给出简洁、准确的回答。")
                .build();
    }

    @Bean("userChatClient")
    public ChatClient userChatClient(ChatClient.Builder builder, UserAITools userTools) {
        return builder
                .defaultSystem("""
                        你是一个智慧校园系统的用户端AI助手，为学生提供学习、阅读、选课等方面的帮助。
                        你可以调用工具来查询学生的成绩、借阅记录、图书信息、课程信息、座位情况等真实数据。
                        当用户询问需要数据支撑的问题时，请主动调用相应的工具获取数据后再进行分析回答。
                        请基于真实数据给出简洁、友好、有针对性的回答。""")
                .defaultTools(userTools)
                .build();
    }

    @Bean("adminChatClient")
    public ChatClient adminChatClient(ChatClient.Builder builder, AdminAITools adminTools) {
        return builder
                .defaultSystem("""
                        你是一个智慧校园管理系统的AI助手，为学校管理员提供专业的数据分析和决策支持。
                        你可以调用工具来查询校园统计数据、学生分布、教师分布、课程分布、成绩数据、借阅趋势、座位利用率等真实数据。
                        当用户询问需要数据支撑的问题时，请主动调用相应的工具获取数据后再进行分析回答。
                        请基于真实数据给出简洁、专业的分析报告和改进建议。""")
                .defaultTools(adminTools)
                .build();
    }
}