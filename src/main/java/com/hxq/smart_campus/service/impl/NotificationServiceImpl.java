package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.pojo.Notification;
import com.hxq.smart_campus.entity.vo.NotificationVO;
import com.hxq.smart_campus.mapper.NotificationMapper;
import com.hxq.smart_campus.service.NotificationService;
import com.hxq.smart_campus.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Async
    public void pushNotification(Notification notification) {
        notificationMapper.insert(notification);
    }

    @Override
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        String userType = SecurityUtils.getCurrentUserType();
        notificationMapper.markAllAsRead(userId, userType);
    }

    @Override
    public List<NotificationVO> getUnreadList() {
        Long userId = SecurityUtils.getCurrentUserId();
        String userType = SecurityUtils.getCurrentUserType();
        return notificationMapper.getUnreadList(userId, userType);
    }

    @Override
    public PageInfo<NotificationVO> getNotificationList(Integer pageNum, Integer pageSize, String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userType = SecurityUtils.getCurrentUserType();
        PageHelper.startPage(pageNum, pageSize);
        List<NotificationVO> list = notificationMapper.getNotificationList(userId, userType, type);
        return new PageInfo<>(list);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        String userType = SecurityUtils.getCurrentUserType();
        return notificationMapper.getUnreadCount(userId, userType);
    }
}