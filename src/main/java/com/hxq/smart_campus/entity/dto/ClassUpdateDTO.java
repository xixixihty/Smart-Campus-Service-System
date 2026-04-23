package com.hxq.smart_campus.entity.dto;

import lombok.Data;

/**
 * 班级更新DTO
 *
 * @author XiongQi He
 * @since 2026-04-22
 */
@Data
public class ClassUpdateDTO {
    private Long id;                // 班级ID，必填
    private String className;       // 班级名称，可选
    private Long headTeacherId;     // 班主任ID，可选
    private String status;          // 状态，可选
}
