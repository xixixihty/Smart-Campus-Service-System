package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class MajorCreateDTO {
    private String id;                // 专业ID
    private Long collegeId;         // 所属学院ID，必填
    private String majorCode;       // 专业代码，必填
    private String majorName;       // 专业名称，必填
    private Integer studyYears;     // 学制年限，必填
}
