package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.AvailableCourseVO;
import com.hxq.smart_campus.entity.vo.CourseRecommendVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.mapper.CourseMapper;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseRecommendService {

    private final CourseMapper courseMapper;
    private final ScoreEntryMapper scoreEntryMapper;
    private final AiService aiService;

    public List<CourseRecommendVO> recommendCourses(Long studentId, Long semesterId) {
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreList(null, studentId, null);
        List<AvailableCourseVO> availableCourses = courseMapper.getAvailableCourseList(semesterId, null);

        if (availableCourses == null || availableCourses.isEmpty()) {
            return Collections.emptyList();
        }

        // 已选/已修课程名集合（带null保护）
        Set<String> takenCourses = scores != null ? scores.stream()
                .map(ScoreEntryListVO::getCourseName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()) : Collections.emptySet();

        // 筛选未修过的课程，按容量利用率排序（已选/容量比越高越热门）
        List<CourseRecommendVO> recommendations = availableCourses.stream()
                .filter(c -> c.getCourseName() != null && !takenCourses.contains(c.getCourseName()))
                .sorted((a, b) -> {
                    double aRate = a.getCapacity() != null && a.getCapacity() > 0
                            ? (double) (a.getSelectedCount() != null ? a.getSelectedCount() : 0) / a.getCapacity()
                            : 0;
                    double bRate = b.getCapacity() != null && b.getCapacity() > 0
                            ? (double) (b.getSelectedCount() != null ? b.getSelectedCount() : 0) / b.getCapacity()
                            : 0;
                    return Double.compare(bRate, aRate);
                })
                .limit(5)
                .map(c -> {
                    double popularity = c.getCapacity() != null && c.getCapacity() > 0
                            ? (double) (c.getSelectedCount() != null ? c.getSelectedCount() : 0) / c.getCapacity()
                            : 0;
                    int rating = (int) Math.round(3 + popularity * 2); // 3-5星
                    return CourseRecommendVO.builder()
                            .id(c.getId())
                            .courseName(c.getCourseName())
                            .courseType(c.getType())
                            .credit(c.getCredit())
                            .teacherName(c.getTeacherName() != null ? c.getTeacherName() : "待定")
                            .reason(popularity > 0.8 ? "热门课程，名额紧张" :
                                    popularity > 0.5 ? "热门选修课程" :
                                    popularity > 0.3 ? "推荐选修" : "新开课程推荐")
                            .rating(rating)
                            .build();
                })
                .collect(Collectors.toList());

        // 调用AI生成个性化推荐理由
        try {
            if (!recommendations.isEmpty()) {
                String allCourses = recommendations.stream()
                        .map(c -> String.format("%s（%s，%s学分）", c.getCourseName(), c.getCourseType(), c.getCredit()))
                        .collect(Collectors.joining("、"));

                String scoreSummary = "";
                if (scores != null && !scores.isEmpty()) {
                    scoreSummary = "已修课程：" + scores.stream()
                            .map(s -> String.format("%s(%s分)", s.getCourseName(), s.getTotalScore()))
                            .collect(Collectors.joining("、"));
                }

                String systemPrompt = "你是一个选课推荐专家。根据学生的已修课程成绩和可选课程列表，"
                        + "为每门推荐课程生成一条简短的个性化推荐理由（20字以内）。"
                        + "请以JSON数组格式返回：[\"推荐理由1\", \"推荐理由2\", ...]";

                String userMessage = String.format("可选课程：%s\n%s\n请为每门课程生成推荐理由。",
                        allCourses, scoreSummary);

                String aiResult = aiService.analyzeWithAdminTools(systemPrompt, userMessage);
                if (aiResult != null && !aiResult.isEmpty()) {
                    // 尝试解析AI返回的推荐理由
                    try {
                        String cleaned = aiResult.trim();
                        if (cleaned.startsWith("```")) {
                            int start = cleaned.indexOf("[");
                            int end = cleaned.lastIndexOf("]");
                            if (start >= 0 && end > start) {
                                cleaned = cleaned.substring(start, end + 1);
                            }
                        }
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String[] reasons = mapper.readValue(cleaned, String[].class);
                        for (int i = 0; i < recommendations.size() && i < reasons.length; i++) {
                            recommendations.get(i).setReason(reasons[i]);
                        }
                    } catch (Exception parseEx) {
                        log.warn("AI推荐理由解析失败，使用默认理由: {}", parseEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("AI课程推荐调用失败，使用默认推荐理由: studentId={}", studentId, e);
        }

        return recommendations;
    }
}
