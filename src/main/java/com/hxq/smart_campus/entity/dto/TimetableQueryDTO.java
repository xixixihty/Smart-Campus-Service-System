package com.hxq.smart_campus.entity.dto;

import lombok.Data;

@Data
public class TimetableQueryDTO {
    private Long semesterId;        // 学期ID，必填
    private Long userId;            // 用户ID，必填
    private String userType;        // 用户类型（teacher/student），必填
}
