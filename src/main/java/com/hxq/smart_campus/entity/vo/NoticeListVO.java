package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class NoticeListVO {
    private Long id;                // 通知ID
    private String title;           // 标题
    private String publisherName;   // 发布人姓名
    private String publishTime;     // 发布时间（格式化显示）
    private String type;            // 通知类型
    private String scope;           // 发布范围
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
}