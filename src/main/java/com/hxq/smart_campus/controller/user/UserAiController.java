package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ai.*;
import com.hxq.smart_campus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/user")
@Tag(name = "用户端AI服务")
@RequiredArgsConstructor
@Slf4j
public class UserAiController {

    private final BookRecommendService bookRecommendService;
    private final ReadingAnalysisService readingAnalysisService;
    private final ScoreAnalysisService scoreAnalysisService;
    private final CourseRecommendService courseRecommendService;

    @GetMapping("/book-recommend")
    @Operation(summary = "图书推荐（基于个人借阅历史）")
    public Result<String> recommendBooks() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("AI图书推荐，用户ID：{}", userId);
        String result = bookRecommendService.recommendByStudent(userId);
        return Result.success(result);
    }

    @GetMapping("/book-recommend/hot")
    @Operation(summary = "热门图书推荐")
    public Result<String> recommendHotBooks() {
        log.info("AI热门图书推荐");
        String result = bookRecommendService.recommendHotBooks();
        return Result.success(result);
    }

    @GetMapping("/reading-analysis")
    @Operation(summary = "借阅行为分析")
    public Result<String> analyzeReading() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("AI借阅行为分析，用户ID：{}", userId);
        String result = readingAnalysisService.analyzeReadingBehavior(userId);
        return Result.success(result);
    }

    @GetMapping("/score-analysis")
    @Operation(summary = "学习成绩分析")
    public Result<String> analyzeScores() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("AI学习成绩分析，用户ID：{}", userId);
        String result = scoreAnalysisService.analyzeStudentScores(userId);
        return Result.success(result);
    }

    @GetMapping("/course-recommend")
    @Operation(summary = "选修课程推荐")
    public Result<String> recommendCourses(@RequestParam(required = false) Long semesterId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("AI选修课程推荐，用户ID：{}，学期ID：{}", userId, semesterId);
        String result = courseRecommendService.recommendCourses(userId, semesterId);
        return Result.success(result);
    }
}
