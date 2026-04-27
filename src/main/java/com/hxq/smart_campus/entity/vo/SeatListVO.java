package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class SeatListVO {
    private Long id;
    private Long roomId;
    private String seatNumber;
    private String status;
}
