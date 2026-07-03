package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.pojo.Notification;
import com.hxq.smart_campus.entity.vo.NotificationVO;

import java.util.List;

public interface NotificationService {

    void pushNotification(Notification notification);

    void markAsRead(Long id);

    void markAllAsRead();

    List<NotificationVO> getUnreadList();

    /**
     * 获取通知列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param type 通知类型（可选）
     * @return 分页列表
     */
    PageInfo<NotificationVO> getNotificationList(Integer pageNum, Integer pageSize, String type);

    Long getUnreadCount();
}