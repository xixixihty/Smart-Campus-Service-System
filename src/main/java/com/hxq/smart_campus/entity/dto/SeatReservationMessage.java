package com.hxq.smart_campus.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservationMessage implements Serializable {
    private Long seatId;
    private Long userId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reservationNo;
    private String eventType; // RESERVE / CANCEL / CHECK_IN / CHECK_OUT
}
