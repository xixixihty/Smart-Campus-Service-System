package com.hxq.smart_campus.entity.dto;

import lombok.Data;

/**
 * 班级创建DTO
 *
 * @author XiongQi He
 * @since 2026-04-22
 */
@Data
public class ClassCreateDTO {
    private Long majorId;           // 所属专业ID，必填
    private String className;       // 班级名称，必填
    private Long headTeacherId;     // 班主任ID，可选
}
