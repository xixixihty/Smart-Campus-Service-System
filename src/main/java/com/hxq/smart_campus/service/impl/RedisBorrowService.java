package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.mapper.BookMapper;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
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

    private RBloomFilter<String> bookFilter;
    private RBloomFilter<String> userFilter;

    @PostConstruct
    public void initFilters() {
        bookFilter = redissonClient.getBloomFilter("bf:books");
        bookFilter.tryInit(10000L, 0.03);

        userFilter = redissonClient.getBloomFilter("bf:users");
        userFilter.tryInit(20000L, 0.03);

        loadBookIdsToFilter();
        initBorrowCache();
        log.info("图书借阅布隆过滤器初始化完成");
    }

    private void loadBookIdsToFilter() {
        List<Long> bookIds = bookMapper.selectAllBookIds();
        bookIds.forEach(id -> bookFilter.add("book:" + id));
        log.info("图书布隆过滤器加载完成，共 {} 个图书ID", bookIds.size());
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
     * @return -2=已借, -1=库存不足已排队, 1=成功
     */
    public Integer executeBorrow(Long bookId, Long userId) {
        String script = loadScript("lua/borrow_book.lua");
        String stockKey = "book:stock:" + bookId;
        String borrowedKey = "book:borrowed:" + userId;
        String waitingKey = "book:waiting:" + bookId;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Integer.class),
                List.of(stockKey, borrowedKey, waitingKey),
                String.valueOf(bookId), String.valueOf(userId),
                String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 执行归还Lua脚本
     * @return -1=未借该书, 1=归还成功无排队, userId=有候补用户
     */
    public Long executeReturn(Long bookId, Long userId) {
        String script = loadScript("lua/return_book.lua");
        String stockKey = "book:stock:" + bookId;
        String borrowedKey = "book:borrowed:" + userId;
        String waitingKey = "book:waiting:" + bookId;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(stockKey, borrowedKey, waitingKey),
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
