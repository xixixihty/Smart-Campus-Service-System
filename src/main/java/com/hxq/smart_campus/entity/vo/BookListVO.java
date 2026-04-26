package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class BookListVO {
    private Long id;                // 图书ID
    private String isbn;            // ISBN编号
    private String title;           // 书名
    private String author;          // 作者
    private String categoryName;    // 分类名称
    private Integer availableCopies; // 可借册数
    private String status;          // 状态
}
