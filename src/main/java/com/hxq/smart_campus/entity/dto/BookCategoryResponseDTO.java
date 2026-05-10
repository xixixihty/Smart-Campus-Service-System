package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookCategoryResponseDTO {
    private Integer id;             // 分类ID
    private String categoryName;     // 分类名称
    private String categoryCode;     // 分类代码
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
