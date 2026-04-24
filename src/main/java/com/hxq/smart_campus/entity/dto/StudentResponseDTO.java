package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentResponseDTO {
    private Long id;                // 学生ID
    private String studentNo;       // 学号
    private String name;            // 姓名
    private String gender;          // 性别
    private Long classId;           // 所属班级ID
    private String className;       // 班级名称
    private LocalDate enrollmentDate; // 入学日期
    private String status;          // 学籍状态
    private String idCard;          // 身份证号
    private String phone;           // 手机号
    private String accountStatus;   // 账号状态
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
