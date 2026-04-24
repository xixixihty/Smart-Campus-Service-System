package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class ClassroomCreateDTO {
    private String building;        // 教学楼，必填
    private String roomNumber;      // 教室门牌号，必填
    private Integer capacity;       // 座位容量，必填
    private String type;            // 类型，可选
    private String status;          // 状态，可选
}
