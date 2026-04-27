package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class SeatReservationDetailVO {
    private Long id;                // 预约ID
    private Long userId;            // 预约人ID
    private String userName;        // 预约人姓名
    private Long seatId;            // 座位ID
    private String seatNumber;      // 座位编号
    private Long roomId;            // 阅览室ID
    private String roomName;        // 阅览室名称
    private LocalDate date;         // 预约日期
    private LocalTime startTime;    // 开始时间
    private LocalTime endTime;      // 结束时间
    private LocalDateTime leaveTime; // 签退/暂离时间
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
