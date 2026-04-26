package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class BookCategoryListVO {
    private Integer id;             // 分类ID
    private String categoryName;     // 分类名称
    private String createTime;      // 创建时间（格式化显示）
}
