package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class MyNoticeVO {
    private Long id;                // 通知ID
    private String title;           // 标题
    private String publisherName;   // 发布人姓名
    private String publishTime;     // 发布时间（格式化显示）
    private String content;         // 正文内容（摘要）
}
