package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class BookCategoryDetailVO {
    private Integer id;             // 分类ID
    private String categoryName;     // 分类名称
    private String categoryCode;     // 分类代码
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
