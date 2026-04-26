package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class BookCategoryUpdateDTO {
    private Integer id;             // 分类ID，必填
    private String categoryName;     // 分类名称，必填
}
