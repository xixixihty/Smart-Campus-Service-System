package com.hxq.smart_campus.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowMessage implements Serializable {
    private Long bookId;
    private Long userId;
    private String borrowNo;
    private String eventType; // BORROW / RETURN
}
