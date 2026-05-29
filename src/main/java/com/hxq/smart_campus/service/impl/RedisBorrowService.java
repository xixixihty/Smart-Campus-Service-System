package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.mapper.BookMapper;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
import com.hxq.smart_campus.mapper.StudentMapper;
import com.hxq.smart_campus.mapper.TeacherMapper;
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
public class RedisBorrowService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    private RBloomFilter<String> bookFilter;
    private RBloomFilter<String> userFilter;

    @PostConstruct
    public void initFilters() {
        bookFilter = redissonClient.getBloomFilter("bf:books");
        bookFilter.tryInit(10000L, 0.03);

        userFilter = redissonClient.getBloomFilter("bf:users");
        userFilter.tryInit(20000L, 0.03);

        loadBookIdsToFilter();
        loadUserIdsToFilter();
        initAllBookStocks();
        initBorrowCache();
        log.info("图书借阅布隆过滤器初始化完成");
    }

    private void loadBookIdsToFilter() {
        List<Long> bookIds = bookMapper.selectAllBookIds();
        bookIds.forEach(id -> bookFilter.add("book:" + id));
        log.info("图书布隆过滤器加载完成，共 {} 个图书ID", bookIds.size());
    }

    private void loadUserIdsToFilter() {
        List<Long> studentIds = studentMapper.selectAllStudentIds();
        studentIds.forEach(id -> userFilter.add("user:" + id));

        List<Long> teacherIds = teacherMapper.selectAllTeacherIds();
        teacherIds.forEach(id -> userFilter.add("user:" + id));

        log.info("用户布隆过滤器加载完成，学生{}人，教师{}人",
                studentIds.size(), teacherIds.size());
    }

    /**
     * 初始化所有图书库存到Redis，从数据库同步available_copies
     */
    private void initAllBookStocks() {
        List<Map<String, Object>> books = bookMapper.selectAllBookStocks();
        if (books == null || books.isEmpty()) {
            log.info("无图书数据，跳过库存初始化");
            return;
        }
        int successCount = 0;
        int skipCount = 0;
        for (Map<String, Object> book : books) {
            Long id = ((Number) book.get("id")).longValue();
            Object copiesObj = book.get("available_copies");
            if (copiesObj == null) {
                log.warn("图书ID={} 的 available_copies 为 NULL，跳过库存初始化", id);
                skipCount++;
                continue;
            }
            Integer availableCopies = ((Number) copiesObj).intValue();
            initStock(id, availableCopies);
            successCount++;
        }
        log.info("图书库存初始化完成，成功 {} 本，跳过 {} 本", successCount, skipCount);
    }

    /**
     * 初始化借阅缓存：从数据库同步所有"借出中"和"逾期"记录到Redis
     */
    private void initBorrowCache() {
        List<Map<String, Object>> activeRecords = borrowRecordMapper.getActiveBorrowRecords();
        if (activeRecords == null || activeRecords.isEmpty()) {
            log.info("无活跃借阅记录，跳过缓存初始化");
            return;
        }

        // 按用户分组，批量写入Redis
        Map<Long, java.util.List<Long>> userBookMap = new java.util.HashMap<>();
        for (Map<String, Object> record : activeRecords) {
            Long userId = ((Number) record.get("userId")).longValue();
            Long bookId = ((Number) record.get("bookId")).longValue();
            userBookMap.computeIfAbsent(userId, k -> new java.util.ArrayList<>()).add(bookId);
        }

        int totalRecords = 0;
        for (Map.Entry<Long, java.util.List<Long>> entry : userBookMap.entrySet()) {
            Long userId = entry.getKey();
            java.util.List<Long> bookIds = entry.getValue();
            String borrowedKey = "book:borrowed:" + userId;
            
            // 使用Set批量添加
            String[] bookIdStrs = bookIds.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(borrowedKey, bookIdStrs);
            totalRecords += bookIds.size();
        }

        log.info("借阅缓存初始化完成，共 {} 条活跃借阅记录，涉及 {} 个用户", totalRecords, userBookMap.size());
    }

    public boolean checkBookExists(Long bookId) {
        return bookFilter.contains("book:" + bookId);
    }

    public boolean checkUserExists(Long userId) {
        return userFilter.contains("user:" + userId);
    }

    public void addUserToFilter(Long userId) {
        userFilter.add("user:" + userId);
    }

    /**
     * 初始化图书库存到Redis
     */
    public void initStock(Long bookId, Integer availableCopies) {
        String stockKey = "book:stock:" + bookId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(availableCopies));
    }

    /**
     * 执行借阅Lua脚本
     * @return -2=已借, -1=库存不足, 1=成功
     */
    public Long executeBorrow(Long bookId, Long userId) {
        String script = loadScript("lua/borrow_book.lua");
        String stockKey = "book:stock:" + bookId;
        String borrowedKey = "book:borrowed:" + userId;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(stockKey, borrowedKey),
                String.valueOf(bookId), String.valueOf(userId));
    }

    /**
     * 执行归还Lua脚本
     * @return -1=未借该书, 1=归还成功
     */
    public Long executeReturn(Long bookId, Long userId) {
        String script = loadScript("lua/return_book.lua");
        String stockKey = "book:stock:" + bookId;
        String borrowedKey = "book:borrowed:" + userId;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(stockKey, borrowedKey),
                String.valueOf(bookId), String.valueOf(userId));
    }

    private String loadScript(String path) {
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
