package com.hxq.smart_campus.entity.vo;

import lombok.Data;

@Data
public class AdminListVO {
    private Long id;
    private String adminNo;
    private String name;
    private String gender;
    private String role;
    private String collegeName;
    private String accountStatus;
    private String createTime;
}