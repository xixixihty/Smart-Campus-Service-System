package com.hxq.smart_campus.config;

import com.hxq.smart_campus.model.AlibabaOpenAiChatModel;
import com.hxq.smart_campus.service.ai.AdminAITools;
import com.hxq.smart_campus.service.ai.TeacherAITools;
import com.hxq.smart_campus.service.ai.UserAITools;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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

    private static final String MARKDOWN_INSTRUCTION =
            "\n\n所有回答请使用Markdown格式输出，包含表格、列表、标题等结构化展示。" +
            "数据查询结果优先使用Markdown表格展示，使信息清晰易读。";

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

    // === 三个独立的 ChatMemory，按角色隔离 ===

    @Bean("userChatMemory")
    public ChatMemory userChatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    @Bean("adminChatMemory")
    public ChatMemory adminChatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    @Bean("teacherChatMemory")
    public ChatMemory teacherChatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    // === 三个 ChatClient，各自绑定独立的 ChatMemory ===

    @Bean("userChatClient")
    public ChatClient userChatClient(ChatClient.Builder builder, UserAITools userTools) {
        return builder
                .defaultSystem(
                        "你是一个智慧校园系统的用户端AI助手，为学生提供学习、阅读、选课等方面的帮助。" +
                        "你可以调用工具来查询学生的成绩、借阅记录、图书信息、课程信息、座位情况等真实数据。" +
                        "当用户询问需要数据支撑的问题时，请主动调用相应的工具获取数据后再进行分析回答。" +
                        "请基于真实数据给出简洁、友好、有针对性的回答。" +
                        MARKDOWN_INSTRUCTION)
                .defaultTools(userTools)
                .build();
    }

    @Bean("adminChatClient")
    public ChatClient adminChatClient(ChatClient.Builder builder, AdminAITools adminTools) {
        return builder
                .defaultSystem(
                        "你是一个智慧校园管理系统的AI助手，为学校管理员提供专业的数据分析和决策支持。" +
                        "你可以调用工具来查询校园统计数据、学生分布、教师分布、课程分布、成绩数据、借阅趋势、座位利用率等真实数据。" +
                        "当用户询问需要数据支撑的问题时，请主动调用相应的工具获取数据后再进行分析回答。" +
                        "请基于真实数据给出简洁、专业的分析报告和改进建议。" +
                        MARKDOWN_INSTRUCTION)
                .defaultTools(adminTools)
                .build();
    }

    @Bean("teacherChatClient")
    public ChatClient teacherChatClient(ChatClient.Builder builder, TeacherAITools teacherTools) {
        return builder
                .defaultSystem(
                        "你是一个智慧校园系统的教师端AI助手，为教师提供教学管理和数据分析方面的帮助。" +
                        "你可以调用工具来查询教师的课表、授课班级、学生成绩、请假审批、调课记录等真实数据。" +
                        "当教师询问需要数据支撑的问题时，请主动调用相应的工具获取数据后再进行分析回答。" +
                        "请基于真实数据给出简洁、实用、有针对性的回答，帮助教师提高教学管理效率。" +
                        MARKDOWN_INSTRUCTION)
                .defaultTools(teacherTools)
                .build();
    }
}