package com.hxq.smart_campus.entity.dto;

import lombok.Data;

/**
 * 学院创建DTO
 *
 * @author XiongQi He
 * @since 2026-04-21
 */
@Data
public class CollegeCreateDTO {

    private String collegeCode;

    private String collegeName;

    private String dean;

    private String contactPhone;
}