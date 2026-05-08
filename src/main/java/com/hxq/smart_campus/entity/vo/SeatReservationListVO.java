package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class SeatReservationListVO {
    private Long id;                // 预约ID
    private String userName;        // 预约人姓名
    private Long seatId;            // 座位ID
    private String seatNumber;      // 座位编号
    private Long roomId;            // 阅览室ID
    private LocalDate date;         // 预约日期
    private LocalTime startTime;    // 开始时间
    private LocalTime endTime;      // 结束时间
    private LocalDateTime leaveTime; // 暂离/签退时间
    private String status;          // 状态
    private String reservationNo;   // 预约编号
}
