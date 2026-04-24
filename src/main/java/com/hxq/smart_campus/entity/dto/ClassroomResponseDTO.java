package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassroomResponseDTO {
    private Long id;                // 教室ID
    private String building;        // 教学楼
    private String roomNumber;      // 教室门牌号
    private Integer capacity;       // 座位容量
    private String type;            // 类型
    private String status;          // 状态
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
