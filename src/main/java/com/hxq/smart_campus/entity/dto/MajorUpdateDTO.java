package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class MajorUpdateDTO {
    private Long id;                // 专业ID，必填
    private String majorName;       // 专业名称，可选
    private Integer studyYears;     // 学制年限，可选
    private String status;          // 状态，可选
}
