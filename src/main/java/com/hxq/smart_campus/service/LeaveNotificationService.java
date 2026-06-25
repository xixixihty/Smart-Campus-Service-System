package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.pojo.Notification;

public interface LeaveNotificationService {

    void notifyLeaveApply(Notification notification);

    void notifyLeaveApproved(Notification notification);

    void notifyLeaveRejected(Notification notification);

    void notifyNeedReschedule(Notification notification);

    void notifyCourseRescheduled(Notification notification);

    /**
     * 通用通知推送（图书逾期、座位提醒等）
     */
    void notifyGeneral(Notification notification);

    void sendToUser(Long userId, String userType, String destination, Object payload);
}