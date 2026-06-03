package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class NotificationVO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private Integer isRead;
    private String createTime;
}