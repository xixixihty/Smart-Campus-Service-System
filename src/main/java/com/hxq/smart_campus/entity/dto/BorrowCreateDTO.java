package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowCreateDTO {
    private Long userId;
    private Long bookId;
    private LocalDate dueDate;
}
