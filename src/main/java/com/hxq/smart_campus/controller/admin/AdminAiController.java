package com.hxq.smart_campus.controller.admin;

import com.hxq.smart_campus.entity.dto.AiChatDTO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ai.AiService;
import com.hxq.smart_campus.service.ai.CampusStatisticsService;
import com.hxq.smart_campus.service.ai.ScoreAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai/admin")
@Tag(name = "管理端AI服务")
@RequiredArgsConstructor
@Slf4j
public class AdminAiController {

    private final CampusStatisticsService campusStatisticsService;
    private final ScoreAnalysisService scoreAnalysisService;
    private final AiService aiService;

    @GetMapping("/campus-overview")
    @Operation(summary = "校园数据统计分析")
    public Result<String> campusOverview() {
        log.info("AI校园数据统计分析");
        String result = campusStatisticsService.analyzeCampusOverview();
        return Result.success(result);
    }

    @GetMapping("/teaching-quality")
    @Operation(summary = "教学质量评估")
    public Result<String> teachingQuality(@RequestParam Long courseId,
                                          @RequestParam(required = false) Long semesterId) {
        log.info("AI教学质量评估，课程ID：{}，学期ID：{}", courseId, semesterId);
        String result = campusStatisticsService.analyzeTeachingQuality(courseId, semesterId);
        return Result.success(result);
    }

    @GetMapping("/score-analysis")
    @Operation(summary = "班级成绩数据分析")
    public Result<String> classScoreAnalysis(@RequestParam Long classId,
                                             @RequestParam(required = false) Long semesterId) {
        log.info("AI班级成绩分析，班级ID：{}，学期ID：{}", classId, semesterId);
        String result = scoreAnalysisService.analyzeClassScores(classId, semesterId);
        return Result.success(result);
    }

    @GetMapping("/resource-optimization")
    @Operation(summary = "资源利用优化建议")
    public Result<String> resourceOptimization() {
        log.info("AI资源利用优化建议");
        String result = campusStatisticsService.analyzeResourceUtilization();
        return Result.success(result);
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI对话（流式）")
    public Flux<String> chat(@RequestBody AiChatDTO chatDTO) {
        log.info("AI对话请求: context={}, message={}", chatDTO.getContext(), chatDTO.getMessage());
        String systemPrompt = buildSystemPrompt(chatDTO.getContext());
        return aiService.analyzeStream(systemPrompt, chatDTO.getMessage());
    }

    private String buildSystemPrompt(String context) {
        String base = "你是一个智慧校园管理系统的AI助手，为学校管理员提供专业的数据分析和决策支持。请基于你的知识给出简洁、专业的回答。";
        if ("campus".equals(context)) {
            return base + "当前场景：校园数据概览。你可以分析学生、教师、图书、座位等校园整体数据。";
        } else if ("teaching".equals(context)) {
            return base + "当前场景：教学质量评估。你可以分析课程教学质量、教师授课效果等。";
        } else if ("score".equals(context)) {
            return base + "当前场景：成绩数据分析。你可以分析学生成绩分布、成绩趋势、学科对比等。";
        } else if ("resource".equals(context)) {
            return base + "当前场景：资源优化建议。你可以分析教室利用率、座位使用率、图书借阅率等资源数据。";
        }
        return base;
    }
}
