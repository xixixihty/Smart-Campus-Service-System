package com.hxq.smart_campus.entity.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ClassResponseDTO {
    private Long id;                // 班级ID
    private Long majorId;           // 所属专业ID
    private String majorName;       // 专业名称
    private String className;       // 班级名称
    private Long headTeacherId;     // 班主任ID
    private String headTeacherName; // 班主任姓名
    private String status;          // 状态
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 修改时间
}
