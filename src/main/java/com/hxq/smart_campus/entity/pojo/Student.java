package com.hxq.smart_campus.entity.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Student {
    private Long id;
    private String studentNo;
    private String name;
    private String gender;
    private Long classId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date enrollmentDate;
    private String status;
    private String idCard;
    private String phone;
    private String password;
    private String accountStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
