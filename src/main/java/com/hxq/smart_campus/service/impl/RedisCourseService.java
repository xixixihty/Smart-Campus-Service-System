package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.mapper.CourseMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisCourseService {
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    private final CourseMapper courseMapper;

    private RBloomFilter<String> courseFilter;

    /**
     * 初始化课程布隆过滤器
     */
    @PostConstruct
    public void initCourseFilter() {
        courseFilter = redissonClient.getBloomFilter("bf:courses");
        courseFilter.tryInit(5000L, 0.03);
        loadCourseToBloomFilter();
    }

    /**
     * 从数据库加载所有课程ID到布隆过滤器
     */
    private void loadCourseToBloomFilter() {
        // 从数据库加载所有课程ID到布隆过滤器
        List<Long> courseIds = loadAllCourseIds();
        courseIds.forEach(id -> courseFilter.add("course:" + id));
        log.info("课程布隆过滤器初始化完成，共加载 {} 个课程ID", courseIds.size());
    }

    /**
     * 检查课程是否存在
     * @param courseId 课程ID
     * @return 是否存在
     */
    public boolean checkCourseExists(Long courseId) {
        return courseFilter.contains("course:" + courseId);
    }

    /**
     * 初始化课程库存
     * @param courseId 课程ID
     * @param capacity 库存容量
     */
    public void initStock(Long courseId, Integer capacity) {
        String stockKey = "course:stock:" + courseId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(capacity));
    }

    /**
     * 执行课程选择
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return 选择结果，0表示失败，1表示成功
     */
    public Integer executeSelection(Long courseId, Long userId) {
        String script = loadScriptFromResource("lua/select_course.lua");
        String stockKey = "course:stock:" + courseId;
        String selectedKey = "course:selected:" + userId;
        String waitingKey = "course:waiting:" + courseId;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Integer.class),
            List.of(stockKey, selectedKey, waitingKey),
            String.valueOf(courseId), String.valueOf(userId),
            String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 退课
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return 退课结果，0表示失败，1表示成功
     */
    public String executeDrop(Long courseId, Long userId) {
        String script = loadScriptFromResource("lua/drop_course.lua");
        String stockKey = "course:stock:" + courseId;
        String selectedKey = "course:selected:" + userId;
        String waitingKey = "course:waiting:" + courseId;
        return redisTemplate.execute(new DefaultRedisScript<>(script, String.class),
                List.of(stockKey, selectedKey, waitingKey),
                String.valueOf(courseId), String.valueOf(userId),
                String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 检查课程是否已选
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否已选
     */
    public boolean isCourseSelected(Long userId, Long courseId) {
        String selectedKey = "course:selected:" + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(selectedKey, String.valueOf(courseId)));
    }

    /**
     * 获取课程库存
     * @param courseId 课程ID
     * @return 库存数量
     */
    public Integer getStock(Long courseId) {
        String stockKey = "course:stock:" + courseId;
        String stock = redisTemplate.opsForValue().get(stockKey);
        return stock != null ? Integer.parseInt(stock) : 0;
    }

    /**
     * 从数据库加载所有课程ID
     * @return
     */
    private List<Long> loadAllCourseIds() {
        // TODO: 调用CourseMapper获取所有课程ID
        // 查询所有课程ID
        List<Long> courseIds = courseMapper.selectAllCourseIds();
        // 从数据库加载所有课程ID
        return courseIds;
    }
    private String loadScriptFromResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("无法加载脚本文件: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取脚本文件失败: " + path, e);
        }
    }




}
