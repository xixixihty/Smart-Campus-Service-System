package com.hxq.smart_campus.entity.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MakeupExam {
    private Long id;
    private Long scoreEntryId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate;
    private String location;
    private String status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
