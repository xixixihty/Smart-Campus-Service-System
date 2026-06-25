package com.hxq.smart_campus.service.impl;

import com.alibaba.fastjson2.JSON;
import com.hxq.smart_campus.entity.pojo.Notification;
import com.hxq.smart_campus.service.LeaveNotificationService;
import com.hxq.smart_campus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveNotificationServiceImpl implements LeaveNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long NOTIFICATION_TTL_DAYS = 30;

    @Override
    public void notifyLeaveApply(Notification notification) {
        notification.setType(NOTIFY_BIZ_LEAVE_APPLY);
        handleNotification(notification, "/queue/leave/apply");
    }

    @Override
    public void notifyLeaveApproved(Notification notification) {
        notification.setType(NOTIFY_BIZ_LEAVE_APPROVED);
        handleNotification(notification, "/queue/leave/approved");
    }

    @Override
    public void notifyLeaveRejected(Notification notification) {
        notification.setType(NOTIFY_BIZ_LEAVE_REJECTED);
        handleNotification(notification, "/queue/leave/rejected");
    }

    @Override
    public void notifyNeedReschedule(Notification notification) {
        notification.setType(NOTIFY_BIZ_NEED_RESCHEDULE);
        handleNotification(notification, "/queue/leave/reschedule");
    }

    @Override
    public void notifyCourseRescheduled(Notification notification) {
        notification.setType(NOTIFY_BIZ_COURSE_RESCHEDULED);
        handleNotification(notification, "/queue/course/rescheduled");
    }

    @Override
    public void notifyGeneral(Notification notification) {
        if (notification.getType() == null || notification.getType().isEmpty()) {
            notification.setType(NOTIFY_BIZ_GENERAL);
        }
        handleNotification(notification, "/queue/notification");
    }

    @Override
    public void sendToUser(Long userId, String userType, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, payload);
        log.debug("WebSocket推送: userId={}, userType={}, dest={}", userId, userType, destination);
    }

    private void handleNotification(Notification notification, String destination) {
        notificationService.pushNotification(notification);

        String key = "ws:notification:" + notification.getUserId() + ":" + notification.getUserType() + ":" + notification.getType() + ":" + notification.getRelatedId();
        redisTemplate.opsForValue().set(key, JSON.toJSONString(notification), NOTIFICATION_TTL_DAYS, TimeUnit.DAYS);

        sendToUser(notification.getUserId(), notification.getUserType(), destination, notification);
    }
}