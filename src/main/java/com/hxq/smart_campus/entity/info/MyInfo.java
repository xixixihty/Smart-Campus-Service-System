package com.hxq.smart_campus.entity.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户信息实体类
 */

@Data
public class MyInfo {
    private Long id;    //  用户ID
    private String name;    //  姓名
    private String userNo;    //  学号或工号
    private String gender;    //  性别
    private String className;    //  班级，学生时显示
    private String majorName;    //  专业，学生时显示
    private String collegeName;    //  学院
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime enrollmentDate;    //  入学时间，学生时显示
    private String phone;    //  电话
    private String email;    //  邮箱，教师时显示
    private String accountStatus;   //  账号状态
    private String status;    //  状态
    private String idCard;    //  身份证号，学生时显示
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;    //  创建时间
}
