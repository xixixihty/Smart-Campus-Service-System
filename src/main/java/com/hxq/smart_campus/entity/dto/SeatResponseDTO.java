package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class SeatResponseDTO {
    private Long id;
    private Long roomId;
    private String seatNumber;
    private String status;
}
