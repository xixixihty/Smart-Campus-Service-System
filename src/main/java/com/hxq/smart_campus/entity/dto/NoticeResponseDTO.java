package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeResponseDTO {
    private Long id;                // 通知ID
    private String title;           // 标题
    private String content;         // 正文内容
    private Long publisherId;       // 发布人ID
    private String publisherName;   // 发布人姓名
    private LocalDateTime publishTime; // 发布时间
    private String targetType;      // 发布范围类型
    private Long targetId;          // 发布范围ID
    private String targetName;      // 发布范围名称
    private String status;          // 状态
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
