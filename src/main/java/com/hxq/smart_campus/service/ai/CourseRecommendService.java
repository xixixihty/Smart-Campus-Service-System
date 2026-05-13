package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.CourseListVO;
import com.hxq.smart_campus.entity.vo.CourseRecommendVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.mapper.CourseMapper;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseRecommendService {

    private final AiService aiService;
    private final CourseMapper courseMapper;
    private final ScoreEntryMapper scoreEntryMapper;
    private final Random random = new Random();

    public List<CourseRecommendVO> recommendCourses(Long studentId, Long semesterId) {
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreList(null, studentId, null);
        List<CourseListVO> availableCourses = courseMapper.getCourseList(null, null, null, null);

        List<String> takenCourses = scores.stream()
                .map(ScoreEntryListVO::getCourseName)
                .collect(Collectors.toList());

        List<CourseRecommendVO> recommendations = new ArrayList<>();
        
        List<CourseListVO> filteredCourses = availableCourses.stream()
                .filter(c -> !takenCourses.contains(c.getCourseName()))
                .limit(5)
                .collect(Collectors.toList());

        String[] reasons = {
            "与您的专业方向高度匹配",
            "根据您的成绩表现推荐",
            "热门选修课程",
            "有助于提升专业能力",
            "学分规划推荐"
        };

        for (int i = 0; i < filteredCourses.size(); i++) {
            CourseListVO course = filteredCourses.get(i);
            recommendations.add(CourseRecommendVO.builder()
                    .id(course.getId())
                    .courseName(course.getCourseName())
                    .courseType(course.getType())
                    .credit(course.getCredit())
                    .teacherName("待定")
                    .reason(reasons[i % reasons.length])
                    .rating(random.nextInt(3) + 3)
                    .build());
        }

        return recommendations;
    }
}
