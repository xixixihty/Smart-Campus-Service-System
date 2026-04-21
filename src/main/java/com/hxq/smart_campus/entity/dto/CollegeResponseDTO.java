package com.hxq.smart_campus.entity.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class CollegeResponseDTO {
    private Long id;                // 学院ID
    private String collegeCode;     // 学院代码
    private String collegeName;     // 学院名称
    private String dean;            // 院长
    private String contactPhone;    // 联系电话
    private String status;          // 状态
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 修改时间
}
