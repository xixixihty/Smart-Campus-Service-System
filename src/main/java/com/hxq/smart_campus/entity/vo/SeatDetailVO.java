package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class SeatDetailVO {
    private Long id;                // 座位ID
    private Long roomId;            // 阅览室ID
    private String seatNumber;      // 座位编号
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
    private String updateTime;      // 修改时间（格式化显示）
}
