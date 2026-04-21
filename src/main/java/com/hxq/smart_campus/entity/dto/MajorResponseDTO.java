package com.hxq.smart_campus.entity.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class MajorResponseDTO {
    private Long id;                // 专业ID
    private Long collegeId;         // 所属学院ID
    private String collegeName;     // 学院名称
    private String majorCode;       // 专业代码
    private String majorName;       // 专业名称
    private Integer studyYears;     // 学制年限
    private String status;          // 状态
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 修改时间
}
