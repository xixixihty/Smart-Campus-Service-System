package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.vo.TeacherDashboardVO;
import com.hxq.smart_campus.entity.vo.ScoreDistributionItem;
import com.hxq.smart_campus.mapper.TeacherDashboardMapper;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherDashboardService {

    private final TeacherDashboardMapper teacherDashboardMapper;
    private final ScoreEntryMapper scoreEntryMapper;
    private final SemesterService semesterService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DASHBOARD_CACHE_PREFIX = "dashboard:teacher:";
    private static final long DASHBOARD_CACHE_TTL = 5; // 5分钟

    public TeacherDashboardVO getTeacherDashboardStats(Long semesterId) {
        Long teacherId = SecurityUtils.getCurrentUserId();

        // 尝试从缓存获取
        String cacheKey = DASHBOARD_CACHE_PREFIX + teacherId + ":" + semesterId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, TeacherDashboardVO.class);
            }
        } catch (Exception e) {
            log.warn("读取Dashboard缓存失败，将查询数据库: {}", e.getMessage());
        }

        TeacherDashboardVO result = new TeacherDashboardVO();

        // 设置当前学期名称
        try {
            result.setSemesterName(semesterService.getCurrentSemester().getName());
        } catch (Exception e) {
            log.warn("获取当前学期名称失败: {}", e.getMessage());
            result.setSemesterName("未知学期");
        }

        result.setTeachingCourseCount(
            teacherDashboardMapper.getTeachingCourseCount(teacherId, semesterId)
        );
        result.setTeachingClassCount(
            teacherDashboardMapper.getTeachingClassCount(teacherId, semesterId)
        );
        result.setTeachingStudentCount(
            teacherDashboardMapper.getTeachingStudentCount(teacherId, semesterId)
        );
        result.setPendingLeaveCount(
            teacherDashboardMapper.getPendingLeaveCount(teacherId)
        );
        result.setPendingRescheduleCount(
            teacherDashboardMapper.getPendingRescheduleCount(teacherId)
        );

        BigDecimal entryRate = teacherDashboardMapper.getScoreEntryRate(teacherId, semesterId);
        if (entryRate != null) {
            entryRate = entryRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        } else {
            entryRate = BigDecimal.ZERO;
        }
        result.setScoreEntryRate(entryRate);

        result.setClassStudentDistribution(
            teacherDashboardMapper.getClassStudentDistribution(teacherId, semesterId)
        );

        List<ScoreDistributionItem> distribution = scoreEntryMapper.getScoreDistributionByTeacher(teacherId, semesterId);
        fillPercentage(distribution, result.getTeachingStudentCount());
        result.setScoreDistribution(distribution);

        // 写入缓存
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(result), DASHBOARD_CACHE_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入Dashboard缓存失败: {}", e.getMessage());
        }

        return result;
    }

    private void fillPercentage(List<ScoreDistributionItem> distribution, Integer totalStudents) {
        if (distribution == null || totalStudents == null || totalStudents <= 0) {
            return;
        }
        int total = distribution.stream().mapToInt(ScoreDistributionItem::getCount).sum();
        if (total <= 0) {
            return;
        }
        for (ScoreDistributionItem item : distribution) {
            BigDecimal percentage = BigDecimal.valueOf(item.getCount())
                    .multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            item.setPercentage(percentage);
        }
    }
}