package com.hxq.smart_campus.entity.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class SeatReservation {
    private Long id;
    private String reservationNo;
    private Long userId;
    private Long seatId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveTime;
    private String status;
}
