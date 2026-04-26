package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookUpdateDTO {
    private Long id;                // 图书ID，必填
    private String isbn;            // ISBN编号，可选
    private String title;           // 书名，可选
    private String author;          // 作者，可选
    private String publisher;       // 出版社，可选
    private LocalDate publishDate;  // 出版日期，可选
    private Integer categoryId;     // 分类ID，可选
    private Integer totalCopies;    // 总册数，可选
    private String status;          // 状态，可选
    private String coverImage;      // 封面图片URL，可选
    private String description;     // 简介，可选
}
