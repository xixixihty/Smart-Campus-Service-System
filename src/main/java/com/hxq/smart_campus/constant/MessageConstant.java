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
     * 用户类型教师
     */
    public static final String USER_TYPE_TEACHER = "teacher";
    /**
     * 用户类型学生
     */
    public static final String USER_TYPE_STUDENT = "student";
    /**
     * 用户类型管理员
     */
    public static final String USER_TYPE_ADMIN = "admin";

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


    /**
     * 座位状态空闲
     */
    public static final String SEAT_STATUS_FREE = "空闲";
    /**
     * 座位状态使用中
     */
    public static final String SEAT_STATUS_USING = "使用中";
    /**
     * 座位状态暂离
     */
    public static final String SEAT_STATUS_TEMPORARILY_ABSENT = "暂离";
    /**
     * 座位状态维修中
     */
    public static final String SEAT_STATUS_REPAIRING = "维修中";


    /**
     * 选课状态已选
     */
    public static final String COURSE_SELECTION_STATUS_SELECTED = "已选";
    /**
     * 选课状态退课
     */
    public static final String COURSE_SELECTION_STATUS_DROPPED_OUT = "退课";


    /**
     * 考试状态正常
     */
    public static final String EXAM_STATUS_NORMAL = "正常";
    /**
     * 考试状态缓考
     */
    public static final String EXAM_STATUS_DELAYED = "缓考";
    /**
     * 考试状态缺考
     */
    public static final String EXAM_STATUS_ABSENT = "缺考";
    /**
     * 考试状态补考
     */
    public static final String EXAM_STATUS_MAKE_UP = "补考";
    /**
     * 考试状态作弊
         */
    public static final String EXAM_STATUS_CHEATING = "作弊";





    /**
     * 补考状态待考
     */
    public static final String MAKEUP_EXAM_STATUS_WAITING = "待考";
    /**
     * 补考状态已考
     */
    public static final String MAKEUP_EXAM_STATUS_PASSED = "已考";
    /**
     * 补考状态取消
     */
    public static final String MAKEUP_EXAM_STATUS_CANCELED = "取消";
    /**
     * 补考状态未通过
     */
    public static final String MAKEUP_EXAM_STATUS_FAILED = "未通过";


    /**
     * 请假申请待审批
     */
    public static final String LEAVE_APPLY_STATUS_WAITING = "待审批";
    /**
     * 请假申请已批准
     */
    public static final String LEAVE_APPLY_STATUS_APPROVED = "已通过";
    /**
     * 请假申请已驳回
     */
    public static final String LEAVE_APPLY_STATUS_REJECTED = "已驳回";
    /**
     * 请假申请已取消
     */
    public static final String LEAVE_APPLY_STATUS_CANCELED = "已取消";


    /**
     * 通知状态发布
     */
    public static final String NOTICE_STATUS_PUBLISHED = "发布";
    /**
     * 通知状态撤回
     */
    public static final String NOTICE_STATUS_RETRACTED = "撤回";

    // ============ 请假申请类型 ============
    /**
     * 请假申请类型-学生
     */
    public static final String APPLICANT_TYPE_STUDENT = "STUDENT";
    /**
     * 请假申请类型-教师
     */
    public static final String APPLICANT_TYPE_TEACHER = "TEACHER";

    // ============ 通知业务类型 ============
    /**
     * 通知类型-新请假申请
     */
    public static final String NOTIFY_BIZ_LEAVE_APPLY = "LEAVE_APPLY";
    /**
     * 通知类型-请假已批准
     */
    public static final String NOTIFY_BIZ_LEAVE_APPROVED = "LEAVE_APPROVED";
    /**
     * 通知类型-请假已驳回
     */
    public static final String NOTIFY_BIZ_LEAVE_REJECTED = "LEAVE_REJECTED";
    /**
     * 通知类型-需要调课
     */
    public static final String NOTIFY_BIZ_NEED_RESCHEDULE = "NEED_RESCHEDULE";
    /**
     * 通知类型-课程已调整
     */
    public static final String NOTIFY_BIZ_COURSE_RESCHEDULED = "COURSE_RESCHEDULED";
    /**
     * 通知类型-图书逾期提醒
     */
    public static final String NOTIFY_BIZ_BOOK_OVERDUE = "BOOK_OVERDUE";
    /**
     * 通知类型-座位预约提醒
     */
    public static final String NOTIFY_BIZ_SEAT_REMINDER = "SEAT_REMINDER";
    /**
     * 通知类型-系统通知
     */
    public static final String NOTIFY_BIZ_GENERAL = "NOTIFICATION";

    // ============ 调课状态 ============
    /**
     * 调课状态-待确认
     */
    public static final String RESCHEDULE_STATUS_PENDING = "待确认";
    /**
     * 调课状态-已确认
     */
    public static final String RESCHEDULE_STATUS_CONFIRMED = "已确认";
    /**
     * 调课状态-已取消
     */
    public static final String RESCHEDULE_STATUS_CANCELED = "已取消";

    // ============ 管理员状态 ============
    /**
     * 管理员账号状态启用
     */
    public static final String ADMIN_ACCOUNT_STATUS_ENABLED = "启用";
    /**
     * 管理员账号状态锁定
     */
    public static final String ADMIN_ACCOUNT_STATUS_LOCK = "锁定";
    /**
     * 管理员账号状态禁用
     */
    public static final String ADMIN_ACCOUNT_STATUS_DISABLED = "禁用";

    // ============ 审批结果 ============
    /**
     * 审批结果-批准
     */
    public static final String APPROVAL_RESULT_APPROVED = "approved";
    /**
     * 审批结果-驳回
     */
    public static final String APPROVAL_RESULT_REJECTED = "rejected";

}
