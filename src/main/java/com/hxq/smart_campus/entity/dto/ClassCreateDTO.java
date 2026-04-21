package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class ClassCreateDTO {
    private Long majorId;           // 所属专业ID，必填
    private String className;       // 班级名称，必填
    private Long headTeacherId;     // 班主任ID，可选
}
