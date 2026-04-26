package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookResponseDTO {
    private Long id;                // 图书ID
    private String isbn;            // ISBN编号
    private String title;           // 书名
    private String author;          // 作者
    private String publisher;       // 出版社
    private LocalDate publishDate;  // 出版日期
    private Integer categoryId;     // 分类ID
    private String categoryName;    // 分类名称
    private Integer totalCopies;    // 总册数
    private Integer availableCopies; // 可借册数
    private String status;          // 状态
    private String coverImage;      // 封面图片URL
    private String description;     // 简介
}
