package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.mapper.BookMapper;
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
public class RedisBorrowService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final BookMapper bookMapper;

    private RBloomFilter<String> bookFilter;
    private RBloomFilter<String> userFilter;

    @PostConstruct
    public void initFilters() {
        bookFilter = redissonClient.getBloomFilter("bf:books");
        bookFilter.tryInit(10000L, 0.03);

        userFilter = redissonClient.getBloomFilter("bf:users");
        userFilter.tryInit(20000L, 0.03);

        loadBookIdsToFilter();
        log.info("图书借阅布隆过滤器初始化完成");
    }

    private void loadBookIdsToFilter() {
        List<Long> bookIds = bookMapper.selectAllBookIds();
        bookIds.forEach(id -> bookFilter.add("book:" + id));
        log.info("图书布隆过滤器加载完成，共 {} 个图书ID", bookIds.size());
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
     * @return -1=未借该书, 1=归还成功无排队, userId字符串=有候补用户
     */
    public String executeReturn(Long bookId, Long userId) {
        String script = loadScript("lua/return_book.lua");
        String stockKey = "book:stock:" + bookId;
        String borrowedKey = "book:borrowed:" + userId;
        String waitingKey = "book:waiting:" + bookId;

        return redisTemplate.execute(new DefaultRedisScript<>(script, String.class),
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
