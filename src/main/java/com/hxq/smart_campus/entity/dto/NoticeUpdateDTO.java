package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class NoticeUpdateDTO {
    private Long id;
    private String title;
    private String content;
    private String targetType;
    private Long targetId;
    private String targetName;
}
