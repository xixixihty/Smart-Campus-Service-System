package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.pojo.Notification;
import com.hxq.smart_campus.entity.vo.NotificationVO;

import java.util.List;

public interface NotificationService {

    void pushNotification(Notification notification);

    void markAsRead(Long id);

    void markAllAsRead();

    List<NotificationVO> getUnreadList();

    List<NotificationVO> getNotificationList(String type);

    Long getUnreadCount();
}