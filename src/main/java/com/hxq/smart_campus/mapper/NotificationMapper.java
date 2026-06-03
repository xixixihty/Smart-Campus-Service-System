package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.pojo.Notification;
import com.hxq.smart_campus.entity.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    int markAsRead(@Param("id") Long id);

    int markAllAsRead(@Param("userId") Long userId,
                      @Param("userType") String userType);

    List<NotificationVO> getUnreadList(@Param("userId") Long userId,
                                       @Param("userType") String userType);

    List<NotificationVO> getNotificationList(@Param("userId") Long userId,
                                         @Param("userType") String userType,
                                         @Param("type") String type);

    Long getUnreadCount(@Param("userId") Long userId,
                        @Param("userType") String userType);

    int deleteByCreateTimeBefore(@Param("threshold") LocalDateTime threshold);
}