package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookCreateDTO {
    private String isbn;            // ISBN编号，必填
    private String title;           // 书名，必填
    private String author;          // 作者，必填
    private String publisher;       // 出版社，可选
    private LocalDate publishDate;  // 出版日期，可选
    private Integer categoryId;     // 分类ID，必填
    private Integer totalCopies;    // 总册数，必填
    private String coverImage;      // 封面图片URL，可选
    private String description;     // 简介，可选
}
