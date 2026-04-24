package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class ClassroomUpdateDTO {
    private Long id;                // 教室ID，必填
    private Integer capacity;       // 座位容量，可选
    private String type;            // 类型，可选
    private String status;          // 状态，可选
}
