package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SelectionTimeRedisVO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
