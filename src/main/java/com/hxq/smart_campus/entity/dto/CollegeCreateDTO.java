package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class CollegeCreateDTO {
    private Long id;                // 学院ID，可选
    private String collegeCode;     // 学院代码，必填
    private String collegeName;     // 学院名称，必填
    private String dean;            // 院长，可选
    private String contactPhone;    // 联系电话，可选
}
