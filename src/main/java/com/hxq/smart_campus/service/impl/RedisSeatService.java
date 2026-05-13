package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.mapper.SeatMapper;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisSeatService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final SeatMapper seatMapper;

    private RBloomFilter<String> seatFilter;

    @PostConstruct
    public void initSeatFilter() {
        seatFilter = redissonClient.getBloomFilter("bf:seats");
        seatFilter.tryInit(5000L, 0.03);
        loadSeatIdsToFilter();
        log.info("座位布隆过滤器初始化完成");
    }

    private void loadSeatIdsToFilter() {
        List<Long> seatIds = seatMapper.selectAllSeatIds();
        seatIds.forEach(id -> seatFilter.add("seat:" + id));
        log.info("座位布隆过滤器加载完成，共 {} 个座位ID", seatIds.size());
    }

    public boolean checkSeatExists(Long seatId) {
        return seatFilter.contains("seat:" + seatId);
    }

    /**
     * 执行预约Lua脚本
     * @return 0=冲突, 1=成功
     */
    public Long reserveSeat(Long seatId, Long userId, LocalDate date,
                            LocalTime startTime, LocalTime endTime, String reservationNo) {
        String script = loadScript("lua/reserve_seat.lua");
        int startScore = startTime.getHour() * 60 + startTime.getMinute();
        int endScore = endTime.getHour() * 60 + endTime.getMinute();
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String scheduleKey = "seat:schedule:" + seatId + ":" + dateStr;
        String availableKey = "seat:available:" + dateStr;
        String reservedKey = "seat:reserved:" + dateStr;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(scheduleKey, availableKey, reservedKey),
                String.valueOf(startScore), String.valueOf(endScore),
                String.valueOf(seatId), String.valueOf(userId), reservationNo);
    }

    /**
     * 执行取消预约Lua脚本
     * @return -1=未找到, 1=成功
     */
    public Long cancelReservation(Long seatId, Long userId, LocalDate date, LocalTime startTime) {
        String script = loadScript("lua/cancel_reservation.lua");
        int startScore = startTime.getHour() * 60 + startTime.getMinute();
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String scheduleKey = "seat:schedule:" + seatId + ":" + dateStr;
        String availableKey = "seat:available:" + dateStr;
        String reservedKey = "seat:reserved:" + dateStr;

        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(scheduleKey, availableKey, reservedKey),
                String.valueOf(seatId), String.valueOf(userId), String.valueOf(startScore));
    }

    /**
     * 每日开馆前预热座位数据
     */
    public void warmUpDailySeats(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String availableKey = "seat:available:" + dateStr;
        String reservedKey = "seat:reserved:" + dateStr;

        List<Long> allSeatIds = seatMapper.selectAllSeatIds();
        String[] seatIdArray = allSeatIds.stream().map(String::valueOf).toArray(String[]::new);
        redisTemplate.opsForSet().add(availableKey, seatIdArray);
        redisTemplate.delete(reservedKey);

        log.info("每日座位预热完成: date={}, totalSeats={}", dateStr, allSeatIds.size());
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
