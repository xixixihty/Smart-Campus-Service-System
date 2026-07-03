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
import java.util.Map;

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
        initAllCourseStocks();
    }

    /**
     * 从数据库加载所有课程ID到布隆过滤器
     */
    private void loadCourseToBloomFilter() {
        List<Long> courseIds = loadAllCourseIds();
        courseIds.forEach(id -> courseFilter.add("course:" + id));
        log.info("课程布隆过滤器初始化完成，共加载 {} 个课程ID", courseIds.size());
    }

    private void initAllCourseStocks() {
        List<Map<String, Object>> courses = courseMapper.selectOpenCourseStocks();
        int successCount = 0;
        int skipCount = 0;
        for (Map<String, Object> course : courses) {
            Long id = ((Number) course.get("id")).longValue();
            Object capObj = course.get("capacity");
            if (capObj == null) {
                log.warn("课程ID={} 的 capacity 为 NULL，跳过库存初始化", id);
                skipCount++;
                continue;
            }
            Integer capacity = ((Number) capObj).intValue();
            initStock(id, capacity);
            successCount++;
        }
        log.info("课程库存初始化完成，成功 {} 门，跳过 {} 门", successCount, skipCount);
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
     * @return -2=已选, -3=已在候补, -4=候补上限, -1=加入候补, 1=选课成功
     */
    public Long executeSelection(Long courseId, Long userId) {
        String script = loadScriptFromResource("lua/select_course.lua");
        String stockKey = "course:stock:" + courseId;
        String selectedKey = "course:selected:" + userId;
        String waitingKey = "course:waiting:" + courseId;
        String waitingCountKey = "course:waiting:count:" + userId;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
            List.of(stockKey, selectedKey, waitingKey, waitingCountKey),
            String.valueOf(courseId), String.valueOf(userId),
            String.valueOf(System.currentTimeMillis()),
            "3");
    }

    /**
     * 退课
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return "0"=未选, "1"=退课成功无候补, userId=有候补补位
     */
    public String executeDrop(Long courseId, Long userId) {
        String script = loadScriptFromResource("lua/drop_course.lua");
        String stockKey = "course:stock:" + courseId;
        String selectedKey = "course:selected:" + userId;
        String waitingKey = "course:waiting:" + courseId;
        String waitingCountKey = "course:waiting:count:" + userId;
        return redisTemplate.execute(new DefaultRedisScript<>(script, String.class),
                List.of(stockKey, selectedKey, waitingKey, waitingCountKey),
                String.valueOf(courseId), String.valueOf(userId));
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
        // 查询所有课程ID
        List<Long> courseIds = courseMapper.selectAllCourseIds();
        // 从数据库加载所有课程ID
        return courseIds;
    }

    /**
     * 加载Lua脚本
     * @param path
     * @return
     */
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
