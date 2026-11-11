package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 学生端：我的补考信息
 */
@Data
public class MyMakeupSessionVO {
    private Long sessionId;
    private String courseName;
    private LocalDate examDate;
    private LocalTime examStartTime;
    private LocalTime examEndTime;
    private String location;
    private String sessionStatus;
    private String myStatus;
    private BigDecimal makeupScore;
    private String notificationTime;
}