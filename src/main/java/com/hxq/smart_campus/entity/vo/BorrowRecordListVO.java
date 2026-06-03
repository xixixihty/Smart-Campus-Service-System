package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowRecordListVO implements BorrowRecordBase {
    private Long id;                // 借阅记录ID
    private String borrowNo;        // 借阅流水号
    private String userName;        // 借阅人姓名
    private String bookTitle;       // 图书标题
    private LocalDate borrowDate;   // 借书日期
    private LocalDate dueDate;      // 应还日期
    private String status;          // 状态
    private Integer overdueDays;    // 逾期天数
}
