package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 补考场次中学生信息
 */
@Data
public class MakeupExamStudentVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long scoreEntryId;
    private BigDecimal originalScore;
    private String originalStatus;
    private BigDecimal makeupScore;
    private String studentStatus;
    private String notificationTime;
}