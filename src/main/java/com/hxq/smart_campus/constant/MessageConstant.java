package com.hxq.smart_campus.constant;

import java.time.format.DateTimeFormatter;

/**
 * 消息常量类
 */
public class MessageConstant {

    /**
     * 日期时间格式化器
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    /**
     * 学院状态正常
     */
    public static final String COLLEGE_STATUS_ENABLED = "启用";
    /**
     * 学院状态停用
     */
    public static final String COLLEGE_STATUS_DISABLED = "禁用";



    /**
     * 专业状态正常
     */
    public static final String MAJOR_STATUS_ENABLED = "启用";
    /**
     * 专业状态停用
     */
    public static final String MAJOR_STATUS_DISABLED = "禁用";



    /**
     * 班级状态正常
     */
    public static final String CLASS_STATUS_ENABLED = "在读";
    /**
     * 班级状态毕业
     */
    public static final String CLASS_STATUS_GRADUATED = "毕业";



    /**
     * 教师状态在职
     */
    public static final String TEACHER_STATUS_ENABLED = "在职";
    /**
     * 教师状态离职
     */
    public static final String TEACHER_STATUS_DISABLED = "离职";
    /**
     * 教师状态退休
     */
    public static final String TEACHER_STATUS_RETIRE = "退休";
    /**
     * 教师账号状态正常
     */
    public static final String TEACHER_ACCOUNT_STATUS_ENABLED = "启用";
    /**
     * 教师账号状态锁定
     */
    public static final String TEACHER_ACCOUNT_STATUS_LOCK = "锁定";
    /**
     * 教师账号状态禁用
     */
    public static final String TEACHER_ACCOUNT_STATUS_DISABLED = "禁用";



    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "Password123!";


    /**
     * 学生状态在读
     */
    public static final String STUDENT_STATUS_ENABLED = "在读";
     /**
     * 学生状态毕业
     */
    public static final String STUDENT_STATUS_GRADUATED = "毕业";
    /**
     * 学生状态休学
     */
    public static final String STUDENT_STATUS_SUSPENDED = "休学";
    /**
     * 学生状态退学
     */
    public static final String STUDENT_STATUS_DROPPED_OUT = "退学";



    /**
     * 学生账号状态正常
     */
    public static final String STUDENT_ACCOUNT_STATUS_ENABLED = "启用";
    /**
     * 学生账号状态锁定
     */
    public static final String STUDENT_ACCOUNT_STATUS_LOCK = "锁定";
    /**
     * 学生账号状态禁用
     */
    public static final String STUDENT_ACCOUNT_STATUS_DISABLED = "禁用";

}
