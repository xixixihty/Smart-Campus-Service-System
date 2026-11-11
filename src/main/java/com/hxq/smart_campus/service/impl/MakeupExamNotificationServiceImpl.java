package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.pojo.MakeupExamSession;
import com.hxq.smart_campus.entity.pojo.Notification;
import com.hxq.smart_campus.entity.vo.MakeupExamSessionDetailVO;
import com.hxq.smart_campus.entity.vo.MakeupExamStudentVO;
import com.hxq.smart_campus.mapper.MakeupExamSessionMapper;
import com.hxq.smart_campus.mapper.MakeupExamStudentMapper;
import com.hxq.smart_campus.service.MakeupExamNotificationService;
import com.hxq.smart_campus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hxq.smart_campus.constant.MessageConstant.*;

/**
 * 补考通知服务实现
 * 复用现有 NotificationService（DB持久化）+ WebSocket（实时推送）+ Redis（去重）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MakeupExamNotificationServiceImpl implements MakeupExamNotificationService {

    private final MakeupExamSessionMapper sessionMapper;
    private final MakeupExamStudentMapper studentMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String WS_DEST_MAKEUP = "/queue/makeup";
    private static final long REDIS_TTL_DAYS = 30;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Async("notificationExecutor")
    public void notifySessionArranged(Long sessionId) {
        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(sessionId);
        if (session == null) {
            log.warn("补考场次不存在: {}", sessionId);
            return;
        }

        List<MakeupExamStudentVO> students = studentMapper.selectBySessionId(sessionId);
        if (students == null || students.isEmpty()) {
            log.warn("场次无学生，跳过通知: {}", sessionId);
            return;
        }

        String contentTemplate = "同学你好，你有一门课程需要参加补考，安排如下：\n"
                + "课程：%s\n"
                + "日期：%s\n"
                + "时间：%s - %s\n"
                + "地点：%s\n"
                + "请携带学生证提前15分钟到达考场。";

        for (MakeupExamStudentVO student : students) {
            String content = String.format(contentTemplate,
                    session.getCourseName(),
                    session.getExamDate().format(DATE_FMT),
                    session.getExamStartTime(),
                    session.getExamEndTime(),
                    session.getLocation());

            Notification notification = buildNotification(
                    student.getStudentId(), "STUDENT",
                    "补考安排通知 - " + session.getCourseName(),
                    content, NOTIFY_BIZ_MAKEUP_ARRANGED, sessionId);

            sendNotification(notification);
        }

        // 更新学生状态
        studentMapper.batchUpdateStatus(sessionId, STUDENT_STATUS_PENDING, STUDENT_STATUS_NOTIFIED);
        // 更新场次状态
        sessionMapper.updateStatus(sessionId, SESSION_STATUS_NOTIFIED);
        log.info("补考通知发送完成: sessionId={}, 学生数={}", sessionId, students.size());
    }

    @Override
    @Async("notificationExecutor")
    public void notifySessionCanceled(Long sessionId) {
        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(sessionId);
        if (session == null) return;

        List<MakeupExamStudentVO> students = studentMapper.selectBySessionId(sessionId);
        if (students == null || students.isEmpty()) return;

        String contentTemplate = "同学你好，原定于 %s 的 %s 补考已取消，后续安排将另行通知，请关注系统消息。";

        for (MakeupExamStudentVO student : students) {
            String content = String.format(contentTemplate,
                    session.getExamDate().format(DATE_FMT), session.getCourseName());

            Notification notification = buildNotification(
                    student.getStudentId(), "STUDENT",
                    "补考取消通知 - " + session.getCourseName(),
                    content, NOTIFY_BIZ_MAKEUP_CANCELED, sessionId);

            sendNotification(notification);
        }
        log.info("补考取消通知发送完成: sessionId={}", sessionId);
    }

    @Override
    @Async("notificationExecutor")
    public void notifyScorePublished(Long sessionId) {
        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(sessionId);
        if (session == null) return;

        List<MakeupExamStudentVO> students = studentMapper.selectBySessionId(sessionId);
        if (students == null || students.isEmpty()) return;

        for (MakeupExamStudentVO student : students) {
            if (student.getMakeupScore() == null) continue;

            String result = "已通过".equals(student.getStudentStatus()) ? "通过" : "未通过";
            String content = String.format(
                    "同学你好，你的 %s 补考成绩已发布：\n补考成绩：%s 分\n结果：%s\n如有疑问请联系任课教师。",
                    session.getCourseName(), student.getMakeupScore(), result);

            Notification notification = buildNotification(
                    student.getStudentId(), "STUDENT",
                    "补考成绩通知 - " + session.getCourseName(),
                    content, NOTIFY_BIZ_MAKEUP_SCORE, sessionId);

            sendNotification(notification);
        }
        log.info("补考成绩通知发送完成: sessionId={}", sessionId);
    }

    @Scheduled(cron = "0 0 10 * * ?")
    @Override
    public void sendExamReminder() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<MakeupExamSession> sessions = sessionMapper.selectByExamDateAndStatus(tomorrow);
        if (sessions == null || sessions.isEmpty()) return;

        for (MakeupExamSession session : sessions) {
            List<MakeupExamStudentVO> students = studentMapper.selectBySessionId(session.getId());
            if (students == null || students.isEmpty()) continue;

            for (MakeupExamStudentVO student : students) {
                if (!STUDENT_STATUS_NOTIFIED.equals(student.getStudentStatus())) continue;

                String content = String.format(
                        "同学你好，明天（%s）%s 在 %s 将进行 %s 补考，请做好准备，携带学生证按时参加。",
                        session.getExamDate().format(DATE_FMT),
                        session.getExamStartTime(),
                        session.getLocation(),
                        "课程" // 课程名需要额外查询，此处简化
                );

                // 防重复：Redis key 25小时
                sendNotificationWithDedup(
                        buildNotification(student.getStudentId(), "STUDENT",
                                "补考提醒", content, NOTIFY_BIZ_MAKEUP_REMINDER, session.getId()),
                        25, TimeUnit.HOURS);
            }
        }
        log.info("补考提醒定时任务完成: 明天考试场次数={}", sessions.size());
    }

    private Notification buildNotification(Long userId, String userType, String title,
                                           String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setUserType(userType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        return notification;
    }

    private void sendNotification(Notification notification) {
        // 1. 异步写入DB
        notificationService.pushNotification(notification);
        // 2. WebSocket实时推送
        pushToUser(notification);
    }

    private void sendNotificationWithDedup(Notification notification, long ttl, TimeUnit unit) {
        String key = buildRedisKey(notification);
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.debug("通知已发送过，跳过: key={}", key);
            return;
        }
        sendNotification(notification);
        redisTemplate.opsForValue().set(key, "1", ttl, unit);
    }

    private void pushToUser(Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(notification.getUserId()),
                    WS_DEST_MAKEUP,
                    notification);
        } catch (Exception e) {
            log.debug("WebSocket推送失败（用户可能离线）: userId={}", notification.getUserId());
        }
    }

    private String buildRedisKey(Notification notification) {
        return "ws:notification:" + notification.getUserId()
                + ":" + notification.getUserType()
                + ":" + notification.getType()
                + ":" + notification.getRelatedId();
    }
}