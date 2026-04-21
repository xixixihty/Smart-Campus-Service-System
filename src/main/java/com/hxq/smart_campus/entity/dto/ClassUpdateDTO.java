package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class ClassUpdateDTO {
    private Long id;                // 班级ID，必填
    private String className;       // 班级名称，可选
    private Long headTeacherId;     // 班主任ID，可选
    private String status;          // 状态，可选
}
