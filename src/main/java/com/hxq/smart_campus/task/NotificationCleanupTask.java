package com.hxq.smart_campus.task;

import com.hxq.smart_campus.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationCleanupTask {

    private final NotificationMapper notificationMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredNotifications() {
        log.info("开始清理过期通知...");
        int deleted = notificationMapper.deleteByCreateTimeBefore(LocalDateTime.now().minusDays(30));
        log.info("清理过期通知完成，共删除{}条", deleted);
    }
}