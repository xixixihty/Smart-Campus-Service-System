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

        return recommendations;
    }
}
