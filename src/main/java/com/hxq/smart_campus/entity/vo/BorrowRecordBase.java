package com.hxq.smart_campus.entity.vo;

import java.time.LocalDate;

public interface BorrowRecordBase {

    LocalDate getDueDate();

    void setOverdueDays(Integer overdueDays);
}