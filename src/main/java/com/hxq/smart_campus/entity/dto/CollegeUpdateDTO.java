package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class CollegeUpdateDTO {
    private Long id;                // 学院ID，必填
    private String collegeName;     // 学院名称，可选
    private String dean;            // 院长，可选
    private String contactPhone;    // 联系电话，可选
    private String status;          // 状态，可选
}
