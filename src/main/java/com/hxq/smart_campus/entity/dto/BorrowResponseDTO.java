package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowResponseDTO {
    private Long id;                // 借阅记录ID
    private Long userId;            // 借阅人ID
    private String userName;        // 借阅人姓名
    private Long bookId;            // 图书ID
    private String bookTitle;       // 图书标题
    private String bookIsbn;        // 图书ISBN
    private LocalDate borrowDate;   // 借书日期
    private LocalDate dueDate;      // 应还日期
    private LocalDate returnDate;   // 实际归还日期
    private String status;          // 状态
    private Integer overdueDays;    // 逾期天数
}
