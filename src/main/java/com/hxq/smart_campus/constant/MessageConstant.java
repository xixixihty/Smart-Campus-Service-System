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


    /**
     * 教室状态正常
     */
    public static final String CLASSROOM_STATUS_ENABLED = "正常";
    /**
     * 教室状态停用
     */
    public static final String CLASSROOM_STATUS_DISABLED = "停用";
    /**
     * 教室状态维修
     */
    public static final String CLASSROOM_STATUS_REPAIR = "维修";





    /**
     * 课程状态正开课
     */
    public static final String COURSE_STATUS_ENABLED = "正开课";
    /**
     * 课程状态停用
     */
    public static final String COURSE_STATUS_DISABLED = "停用";


    /**
     * 课程状态进行中
     */
    public static final String COURSE_STATUS_IN_PROGRESS = "进行中";
    /**
     * 课程状态已结束
     */
    public static final String COURSE_STATUS_FINISHED = "已结束";
    /**
     * 课程状态未开始
     */
    public static final String COURSE_STATUS_NOT_STARTED = "未开始";
    /**
     * 是否是当前学期 -- 是
     */
    public static final Boolean IS_CURRENT_SEMESTER_YES = true;
    /**
     * 是否是当前学期 -- 否
     */
    public static final Boolean IS_CURRENT_SEMESTER_NO = false;


    /**
     * 图书状态在库
     */
    public static final String BOOK_STATUS_ENABLED = "在库";
    /**
     * 图书状态借出
     */
    public static final String BOOK_STATUS_BORROWED = "借出";
    /**
     * 图书状态维修
     */
    public static final String BOOK_STATUS_REPAIR = "维修";
    /**
     * 图书状态遗失
     */
    public static final String BOOK_STATUS_LOST = "遗失";


    /**
     * 借阅状态借出中
     */
    public static final String BORROW_STATUS_BORROWING = "借出中";
    /**
     * 借阅状态已归还
     */
    public static final String BORROW_STATUS_RETURNED = "已归还";
    /**
     * 借阅状态逾期
     */
    public static final String BORROW_STATUS_OVERDUE = "逾期";

}
