package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowRecord {
    private Long id;
    private String borrowNo;
    private Long userId;
    private Long bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
}
