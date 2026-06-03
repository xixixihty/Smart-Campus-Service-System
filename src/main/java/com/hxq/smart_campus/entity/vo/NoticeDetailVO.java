package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeDetailVO {
    private Long id;                // 通知ID
    private String title;           // 标题
    private String content;         // 正文内容
    private Long publisherId;       // 发布人ID
    private String publisherName;   // 发布人姓名
    private LocalDateTime publishTime; // 发布时间
    private String type;            // 通知类型
    private String scope;           // 发布范围
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}