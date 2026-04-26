package com.hxq.smart_campus.entity.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class Book {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    private Integer categoryId;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status;
    private String coverImage;
    private String description;
}
