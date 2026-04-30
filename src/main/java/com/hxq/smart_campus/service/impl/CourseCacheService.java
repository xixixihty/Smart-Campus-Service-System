package com.hxq.smart_campus.service.impl;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hxq.smart_campus.entity.vo.CourseDetailVO;
import com.hxq.smart_campus.service.CourseService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CourseCacheService {

    private final Cache<String, CourseDetailVO> caffeineCache;
    private final StringRedisTemplate redisTemplate;
    private final CourseService courseService;

    public CourseCacheService(StringRedisTemplate redisTemplate, CourseService courseService) {
        this.redisTemplate = redisTemplate;
        this.courseService = courseService;
        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    public CourseDetailVO getCourseDetail(Long courseId) {
        String key = "course:info:" + courseId;

        // 先查本地缓存
        CourseDetailVO localCache = caffeineCache.getIfPresent(key);
        if (localCache != null) {
            return localCache;
        }

        // 再查Redis
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            CourseDetailVO courseDetailVO = JSONUtil.toBean(json, CourseDetailVO.class);
            caffeineCache.put(key, courseDetailVO);
            return courseDetailVO;
        }

        // 最后查DB
        return loadFromDBAndCache(courseId);
    }

    private CourseDetailVO loadFromDBAndCache(Long courseId) {
        CourseDetailVO courseDetailVO = courseService.getCourseDetail(courseId);
        if (courseDetailVO != null) {
            String key = "course:info:" + courseId;
            String json = JSONUtil.toJsonStr(courseDetailVO);
            redisTemplate.opsForValue().set(key, json, 1, TimeUnit.DAYS);
            caffeineCache.put(key, courseDetailVO);
        }
        return courseDetailVO;
    }

    public void invalidateCache(Long courseId) {
        String key = "course:info:" + courseId;
        caffeineCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
