package com.hxq.smart_campus.entity.pojo;

import lombok.Data;

@Data
public class Seat {
    private Long id;
    private Long roomId;
    private String seatNumber;
    private String status;
}
