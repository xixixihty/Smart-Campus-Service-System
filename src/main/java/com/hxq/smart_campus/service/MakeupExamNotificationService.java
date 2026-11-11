package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.pojo.Notification;

/**
 * 补考通知服务接口
 */
public interface MakeupExamNotificationService {

    /**
     * 通知学生补考已安排
     */
    void notifySessionArranged(Long sessionId);

    /**
     * 通知学生补考已取消
     */
    void notifySessionCanceled(Long sessionId);

    /**
     * 通知学生补考成绩已发布
     */
    void notifyScorePublished(Long sessionId);

    /**
     * 定时任务：考前提醒
     */
    void sendExamReminder();
}