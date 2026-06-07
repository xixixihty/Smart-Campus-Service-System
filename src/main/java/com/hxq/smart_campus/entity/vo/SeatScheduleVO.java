package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SeatScheduleVO {
    private Long seatId;
    private String seatNumber;
    private LocalDate date;
    private List<TimeSlotVO> timeSlots;

    @Data
    public static class TimeSlotVO {
        private String time;
        private Boolean available;
        private String reason;      // 不可用原因: PASSED=已过期, BOOKED=已预约
        private String reservedBy;
    }
}