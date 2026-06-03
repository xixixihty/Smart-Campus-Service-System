package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class NoticeCreateDTO {
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文内容不能为空")
    private String content;

    private Long publisherId;

    @NotBlank(message = "通知类型不能为空")
    private String type;

    @NotBlank(message = "发布范围不能为空")
    private String scope;
}