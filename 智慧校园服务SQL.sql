-- ============================================================================
-- 智慧校园服务系统 数据库初始化脚本（完整重构版 v6.0）
-- 包含：建库、26张表、索引、外键约束（仅DDL，无测试数据）
-- 字段名严格对齐后端 entity/pojo 类
-- 执行方式: mysql -u root -p < 智慧校园服务SQL.md
-- 注意: 请确保 MySQL 客户端使用 utf8mb4 编码连接
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `smart_campus`;
CREATE DATABASE `smart_campus` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smart_campus`;

-- ============================================================================
-- 一、图书模块（6张表）
-- ============================================================================

-- 1. 图书分类表
CREATE TABLE book_category (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    category_name   VARCHAR(50)     NOT NULL                 COMMENT '分类名称',
    category_code   VARCHAR(20)     NOT NULL                 COMMENT '分类代码',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (category_name),
    UNIQUE KEY uk_category_code (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- 2. 图书信息表
CREATE TABLE book (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    isbn              VARCHAR(20)     NOT NULL                 COMMENT 'ISBN编号',
    title             VARCHAR(200)    NOT NULL                 COMMENT '书名',
    author            VARCHAR(100)    NOT NULL                 COMMENT '作者',
    publisher         VARCHAR(100)    DEFAULT NULL             COMMENT '出版社',
    publish_date      DATE            DEFAULT NULL             COMMENT '出版日期',
    category_id       INT UNSIGNED    DEFAULT NULL             COMMENT '分类ID',
    total_copies      INT UNSIGNED    NOT NULL DEFAULT 1       COMMENT '总册数',
    available_copies  INT UNSIGNED    NOT NULL DEFAULT 1       COMMENT '可借册数',
    status            VARCHAR(20)     NOT NULL DEFAULT '正常'  COMMENT '状态',
    cover_image       VARCHAR(500)    DEFAULT NULL             COMMENT '封面图片URL',
    description       TEXT            DEFAULT NULL             COMMENT '简介',
    PRIMARY KEY (id),
    KEY idx_isbn (isbn),
    KEY idx_title (title),
    KEY idx_author (author),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书信息表';

-- 3. 借阅记录表
CREATE TABLE borrow_record (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    borrow_no     VARCHAR(64)     NOT NULL                 COMMENT '借阅编号（UUID，MQ幂等）',
    user_id       BIGINT UNSIGNED NOT NULL                 COMMENT '借阅人ID',
    book_id       BIGINT UNSIGNED NOT NULL                 COMMENT '图书ID',
    borrow_date   DATE            NOT NULL                 COMMENT '借书日期',
    due_date      DATE            NOT NULL                 COMMENT '应还日期',
    return_date   DATE            DEFAULT NULL             COMMENT '实际归还日期',
    status        VARCHAR(20)     NOT NULL DEFAULT '借阅中'  COMMENT '状态',
    PRIMARY KEY (id),
    UNIQUE KEY uk_borrow_no (borrow_no),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (status),
    KEY idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 4. 座位信息表
CREATE TABLE seat (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    room_id     BIGINT UNSIGNED NOT NULL                 COMMENT '阅览室ID',
    seat_number VARCHAR(20)     NOT NULL                 COMMENT '座位编号',
    status      VARCHAR(20)     NOT NULL DEFAULT '空闲'   COMMENT '状态',
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_seat (room_id, seat_number),
    KEY idx_room_id (room_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位信息表';

-- 5. 座位预约表
CREATE TABLE seat_reservation (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    reservation_no  VARCHAR(64)     NOT NULL                 COMMENT '预约编号（UUID，MQ幂等）',
    user_id         BIGINT UNSIGNED NOT NULL                 COMMENT '预约人ID',
    seat_id         BIGINT UNSIGNED NOT NULL                 COMMENT '座位ID',
    date            DATE            NOT NULL                 COMMENT '预约日期',
    start_time      TIME            NOT NULL                 COMMENT '开始时段',
    end_time        TIME            NOT NULL                 COMMENT '结束时段',
    leave_time      DATETIME        DEFAULT NULL             COMMENT '签退/暂离时间',
    status          VARCHAR(20)     NOT NULL DEFAULT '待签到' COMMENT '状态',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sr_reservation_no (reservation_no),
    KEY idx_user_id (user_id),
    KEY idx_seat_id (seat_id),
    KEY idx_date (date),
    KEY idx_status (status),
    KEY idx_seat_date_status (seat_id, date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位预约表';

-- 6. 阅读报告表
CREATE TABLE reading_report (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id       BIGINT UNSIGNED NOT NULL                 COMMENT '用户ID',
    semester      VARCHAR(20)     NOT NULL                 COMMENT '学期',
    total_borrow  INT UNSIGNED    NOT NULL DEFAULT 0       COMMENT '总借阅量',
    fav_category  VARCHAR(50)     DEFAULT NULL             COMMENT '偏好分类',
    analysis_text TEXT            DEFAULT NULL             COMMENT 'AI生成的分析文本',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_semester (user_id, semester),
    KEY idx_user_id (user_id),
    KEY idx_semester (semester),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读报告表';

-- ============================================================================
-- 二、教务系统模块（21张表）
-- ============================================================================

-- 7. 学院表
CREATE TABLE college (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    college_code  VARCHAR(20)     NOT NULL                 COMMENT '学院代码',
    college_name  VARCHAR(100)    NOT NULL                 COMMENT '学院名称',
    dean          VARCHAR(50)     DEFAULT NULL             COMMENT '院长',
    contact_phone VARCHAR(20)     DEFAULT NULL             COMMENT '联系电话',
    status        VARCHAR(20)     NOT NULL DEFAULT '正常'   COMMENT '状态',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_college_code (college_code),
    KEY idx_college_name (college_name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院表';

-- 8. 专业表
CREATE TABLE major (
    id          BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    college_id  BIGINT UNSIGNED   NOT NULL                 COMMENT '所属学院ID',
    major_code  VARCHAR(20)       NOT NULL                 COMMENT '专业代码',
    major_name  VARCHAR(100)      NOT NULL                 COMMENT '专业名称',
    study_years TINYINT UNSIGNED  NOT NULL DEFAULT 4       COMMENT '学制年限',
    status      VARCHAR(20)       NOT NULL DEFAULT '正常'   COMMENT '状态',
    create_time DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_major_code (major_code),
    KEY idx_college_id (college_id),
    KEY idx_major_name (major_name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业表';

-- 9. 班级表
CREATE TABLE class (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    major_id        BIGINT UNSIGNED NOT NULL                 COMMENT '所属专业ID',
    class_name      VARCHAR(50)     NOT NULL                 COMMENT '班级名称',
    head_teacher_id BIGINT UNSIGNED DEFAULT NULL             COMMENT '班主任ID',
    status          VARCHAR(20)     NOT NULL DEFAULT '在读'   COMMENT '状态',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_major_id (major_id),
    KEY idx_class_name (class_name),
    KEY idx_head_teacher_id (head_teacher_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 10. 教师表
CREATE TABLE teacher (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    teacher_no     VARCHAR(20)     NOT NULL                 COMMENT '工号（登录账号）',
    name           VARCHAR(50)     NOT NULL                 COMMENT '姓名',
    gender         VARCHAR(10)     NOT NULL                 COMMENT '性别',
    college_id     BIGINT UNSIGNED NOT NULL                 COMMENT '所属学院ID',
    title          VARCHAR(20)     DEFAULT NULL             COMMENT '职称',
    phone          VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    email          VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    password       VARCHAR(255)    NOT NULL                 COMMENT '登录密码（BCrypt加密）',
    account_status VARCHAR(20)     NOT NULL DEFAULT '正常'   COMMENT '账号状态',
    status         VARCHAR(20)     NOT NULL DEFAULT '在职'   COMMENT '任职状态',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no),
    KEY idx_name (name),
    KEY idx_college_id (college_id),
    KEY idx_status (status),
    KEY idx_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师表';

-- 11. 学生表
CREATE TABLE student (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    student_no      VARCHAR(20)     NOT NULL                 COMMENT '学号（登录账号）',
    name            VARCHAR(50)     NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(10)     NOT NULL                 COMMENT '性别',
    class_id        BIGINT UNSIGNED NOT NULL                 COMMENT '所属班级ID',
    enrollment_date DATE            NOT NULL                 COMMENT '入学日期',
    status          VARCHAR(20)     NOT NULL DEFAULT '在读'   COMMENT '学籍状态',
    id_card         VARCHAR(18)     DEFAULT NULL             COMMENT '身份证号',
    phone           VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    password        VARCHAR(255)    NOT NULL                 COMMENT '登录密码（BCrypt加密）',
    account_status  VARCHAR(20)     NOT NULL DEFAULT '正常'   COMMENT '账号状态',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_name (name),
    KEY idx_class_id (class_id),
    KEY idx_status (status),
    KEY idx_phone (phone),
    KEY idx_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- 12. 管理员表
-- 设计说明：
--   1. college_id 将管理员与学院关联，实现「学院管理员审批本学院教师请假」
--   2. super_admin 角色时 college_id 可空，可审批全院请假（兜底机制）
--   3. 密码采用 BCrypt 哈希存储，与 student/teacher 密码机制一致
CREATE TABLE admin (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '管理员ID',
    admin_no        VARCHAR(32)     NOT NULL                 COMMENT '管理员账号（登录用）',
    name            VARCHAR(50)     NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(10)     NOT NULL DEFAULT '男'    COMMENT '性别',
    role            VARCHAR(20)     NOT NULL DEFAULT '管理员' COMMENT '角色',
    college_id      BIGINT UNSIGNED DEFAULT NULL             COMMENT '所属学院ID（超级管理员为NULL）',
    phone           VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    account_status  VARCHAR(20)     NOT NULL DEFAULT '启用'   COMMENT '账号状态',
    password        VARCHAR(255)    NOT NULL                 COMMENT 'BCrypt哈希密码',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_no (admin_no),
    KEY idx_admin_college (college_id),
    KEY idx_admin_role (role),
    KEY idx_admin_status (account_status),
    KEY idx_admin_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 13. 教室表
CREATE TABLE classroom (
    id          BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    building    VARCHAR(50)       NOT NULL                 COMMENT '教学楼',
    room_number VARCHAR(20)       NOT NULL                 COMMENT '教室门牌号',
    capacity    SMALLINT UNSIGNED NOT NULL                 COMMENT '座位容量',
    type        VARCHAR(20)       NOT NULL DEFAULT '普通'   COMMENT '类型',
    status      VARCHAR(20)       NOT NULL DEFAULT '可用'   COMMENT '状态',
    create_time DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_building_room (building, room_number),
    KEY idx_building (building),
    KEY idx_type (type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室表';

-- 14. 学期表
CREATE TABLE semester (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    name        VARCHAR(30)     NOT NULL                 COMMENT '学期名称',
    start_date  DATE            NOT NULL                 COMMENT '开学日期',
    end_date    DATE            NOT NULL                 COMMENT '结束日期',
    is_current  TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否当前学期',
    status      VARCHAR(20)     NOT NULL DEFAULT '未开始' COMMENT '状态',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_is_current (is_current),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期表';

-- 15. 课程表
CREATE TABLE course (
    id            BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT  COMMENT '主键',
    course_code   VARCHAR(20)         NOT NULL                 COMMENT '课程代码',
    course_name   VARCHAR(100)        NOT NULL                 COMMENT '课程名称',
    credit        DECIMAL(3,1) UNSIGNED NOT NULL               COMMENT '学分',
    hours         TINYINT UNSIGNED    NOT NULL                 COMMENT '总学时',
    type          VARCHAR(20)         NOT NULL                 COMMENT '课程类型',
    status        VARCHAR(20)         NOT NULL DEFAULT '开课'   COMMENT '状态',
    capacity      SMALLINT UNSIGNED   DEFAULT NULL             COMMENT '课程容量',
    week_day      TINYINT UNSIGNED    DEFAULT NULL             COMMENT '上课星期几(1-7)',
    class_start   TINYINT UNSIGNED    DEFAULT NULL             COMMENT '上课开始节次',
    class_end     TINYINT UNSIGNED    DEFAULT NULL             COMMENT '上课结束节次',
    week_start    TINYINT UNSIGNED    DEFAULT NULL             COMMENT '开始周次',
    week_end      TINYINT UNSIGNED    DEFAULT NULL             COMMENT '结束周次',
    schedule_type VARCHAR(20)         DEFAULT NULL             COMMENT '排课类型',
    create_time   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code (course_code),
    KEY idx_course_name (course_name),
    KEY idx_type (type),
    KEY idx_capacity (capacity),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 16. 排课表
CREATE TABLE course_schedule (
    id            BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    semester_id   BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    course_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    teacher_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '授课教师ID',
    classroom_id  BIGINT UNSIGNED   NOT NULL                 COMMENT '教室ID',
    week_day      TINYINT UNSIGNED  NOT NULL                 COMMENT '星期几(1-7)',
    start_section TINYINT UNSIGNED  NOT NULL                 COMMENT '开始节次',
    end_section   TINYINT UNSIGNED  NOT NULL                 COMMENT '结束节次',
    week_range    VARCHAR(20)       NOT NULL                 COMMENT '周次范围',
    PRIMARY KEY (id),
    KEY idx_semester_id (semester_id),
    KEY idx_course_id (course_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_classroom_id (classroom_id),
    KEY idx_week_day (week_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课表';

-- 16.1 排课-班级关联表（多对多，替代 class_ids JSON）
CREATE TABLE course_schedule_class (
    id          BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    schedule_id BIGINT UNSIGNED   NOT NULL                 COMMENT '排课ID',
    class_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '班级ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_class (schedule_id, class_id),
    KEY idx_schedule_id (schedule_id),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课-班级关联表';

-- 17. 选课时间段配置表
CREATE TABLE course_selection_period (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    semester_id BIGINT UNSIGNED NOT NULL                 COMMENT '学期ID',
    start_time  DATETIME        NOT NULL                 COMMENT '选课开始时间',
    end_time    DATETIME        NOT NULL                 COMMENT '选课结束时间',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_semester_id (semester_id),
    KEY idx_start_end_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课时间段配置表';

-- 18. 选课表
CREATE TABLE course_selection (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    student_id  BIGINT UNSIGNED NOT NULL                 COMMENT '学生ID',
    course_id   BIGINT UNSIGNED NOT NULL                 COMMENT '课程ID',
    semester_id BIGINT UNSIGNED NOT NULL                 COMMENT '学期ID',
    status      VARCHAR(20)     NOT NULL DEFAULT '已选'   COMMENT '状态',
    score       DECIMAL(5,2)    DEFAULT NULL             COMMENT '最终成绩',
    score_point DECIMAL(3,2)    DEFAULT NULL             COMMENT '绩点',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester_id),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_semester_id (semester_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课表';

-- 19. 成绩录入表
CREATE TABLE score_entry (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    course_id     BIGINT UNSIGNED NOT NULL                 COMMENT '课程ID',
    student_id    BIGINT UNSIGNED NOT NULL                 COMMENT '学生ID',
    semester_id   BIGINT UNSIGNED NOT NULL                 COMMENT '学期ID',
    usual_score   DECIMAL(5,2)    DEFAULT NULL             COMMENT '平时成绩',
    final_score   DECIMAL(5,2)    DEFAULT NULL             COMMENT '期末成绩',
    total_score   DECIMAL(5,2)    DEFAULT NULL             COMMENT '总评成绩',
    makeup_score  DECIMAL(5,2)    DEFAULT NULL             COMMENT '补考成绩',
    score_point   DECIMAL(3,2)    DEFAULT NULL             COMMENT '绩点',
    exam_status   VARCHAR(20)     NOT NULL DEFAULT '正常'   COMMENT '考试状态',
    makeup_exam_id BIGINT UNSIGNED DEFAULT NULL             COMMENT '关联补考ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester_id),
    KEY idx_course_id (course_id),
    KEY idx_student_id (student_id),
    KEY idx_semester_id (semester_id),
    KEY idx_exam_status (exam_status),
    KEY idx_makeup_exam_id (makeup_exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩录入表';

-- 20. 请假申请表
-- 设计说明：
--   1. applicant_type + applicant_id 取代原先单一的 student_id，支持教师请假
--   2. approver_id 在提交时由系统自动匹配填充
--   3. is_course_rescheduled 标记教师请假是否需要调课
--   4. student_id 保留但改为可空，兼容旧数据
CREATE TABLE leave_request (
    id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    applicant_type       VARCHAR(20)     NOT NULL DEFAULT 'STUDENT' COMMENT '申请人类型：STUDENT / TEACHER',
    applicant_id         BIGINT UNSIGNED NOT NULL                 COMMENT '申请人ID（学生ID或教师ID）',
    approver_id          BIGINT UNSIGNED DEFAULT NULL             COMMENT '审批人ID（教师ID或管理员ID）',
    student_id           BIGINT UNSIGNED DEFAULT NULL             COMMENT '学生ID（仅学生请假时填充）',
    leave_type           VARCHAR(20)     NOT NULL                 COMMENT '请假类型',
    start_time           DATETIME        NOT NULL                 COMMENT '开始时间',
    end_time             DATETIME        NOT NULL                 COMMENT '结束时间',
    reason               VARCHAR(500)    NOT NULL                 COMMENT '请假事由',
    status               VARCHAR(20)     NOT NULL DEFAULT '待审批' COMMENT '状态',
    is_course_rescheduled TINYINT(1)     NOT NULL DEFAULT 0       COMMENT '是否需要调课：0-否，1-是',
    create_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (id),
    KEY idx_applicant (applicant_type, applicant_id),
    KEY idx_approver_id (approver_id),
    KEY idx_student_id (student_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- 21. 请假审批日志表
CREATE TABLE leave_approval_log (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    leave_request_id BIGINT UNSIGNED NOT NULL                 COMMENT '请假申请ID',
    approver_id      BIGINT UNSIGNED NOT NULL                 COMMENT '审批人ID',
    action           VARCHAR(20)     NOT NULL                 COMMENT '审批动作：通过/驳回',
    comment          VARCHAR(500)    DEFAULT NULL             COMMENT '审批意见',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    PRIMARY KEY (id),
    KEY idx_leave_request_id (leave_request_id),
    KEY idx_approver_id (approver_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假审批日志表';

-- 22. 调课申请表
-- 设计说明：
--   1. teacher_id 标识发起调课的教师
--   2. original_* 记录原始排课信息，new_* 记录调整后的信息
--   3. 管理员审批通过后，同步更新 course_schedule 表
--   4. leave_request_id 关联请假申请（可选，教师请假触发调课时使用）
CREATE TABLE course_reschedule (
    id                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '主键',
    course_schedule_id  BIGINT UNSIGNED  NOT NULL                 COMMENT '原排课ID',
    teacher_id          BIGINT UNSIGNED  NOT NULL                 COMMENT '申请调课教师ID',
    original_week_day   TINYINT UNSIGNED NOT NULL                 COMMENT '原星期几',
    original_start_section TINYINT UNSIGNED NOT NULL              COMMENT '原始开始节次',
    original_end_section   TINYINT UNSIGNED NOT NULL              COMMENT '原始结束节次',
    original_week_range VARCHAR(20)      NOT NULL                 COMMENT '原周次范围',
    original_classroom_id BIGINT UNSIGNED NOT NULL                COMMENT '原教室ID',
    new_week_day        TINYINT UNSIGNED NOT NULL                 COMMENT '新星期几',
    new_start_section   TINYINT UNSIGNED NOT NULL                 COMMENT '新开始节次',
    new_end_section     TINYINT UNSIGNED NOT NULL                 COMMENT '新结束节次',
    new_week_range      VARCHAR(20)      NOT NULL                 COMMENT '新周次范围',
    new_classroom_id    BIGINT UNSIGNED  NOT NULL                 COMMENT '新教室ID',
    reason              VARCHAR(500)     NOT NULL                 COMMENT '调课原因',
    status              VARCHAR(20)      NOT NULL DEFAULT '待审批' COMMENT '状态',
    approver_id         BIGINT UNSIGNED  DEFAULT NULL             COMMENT '审批人ID',
    leave_request_id    BIGINT UNSIGNED  DEFAULT NULL             COMMENT '关联请假ID',
    approve_time        DATETIME         DEFAULT NULL             COMMENT '审批时间',
    create_time         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_course_schedule_id (course_schedule_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_status (status),
    KEY idx_approver_id (approver_id),
    KEY idx_leave_request_id (leave_request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调课申请表';

-- 23. 消息通知表
CREATE TABLE notification (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id     BIGINT UNSIGNED NOT NULL                 COMMENT '接收用户ID',
    user_type   VARCHAR(20)     NOT NULL                 COMMENT '用户类型：STUDENT/TEACHER/ADMIN',
    title       VARCHAR(200)    NOT NULL                 COMMENT '通知标题',
    content     TEXT            NOT NULL                 COMMENT '通知内容',
    type        VARCHAR(20)     NOT NULL DEFAULT '系统通知' COMMENT '通知类型',
    is_read     TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否已读',
    related_id  BIGINT UNSIGNED DEFAULT NULL             COMMENT '关联业务ID',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id, user_type),
    KEY idx_is_read (is_read),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 24. 公告表
CREATE TABLE notice (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    title        VARCHAR(200)    NOT NULL                 COMMENT '公告标题',
    content      TEXT            NOT NULL                 COMMENT '公告内容',
    publisher_id BIGINT UNSIGNED NOT NULL                 COMMENT '发布人ID（教师ID）',
    type         VARCHAR(20)     NOT NULL DEFAULT '全校公告' COMMENT '公告类型',
    scope        VARCHAR(20)     NOT NULL DEFAULT '全部'   COMMENT '可见范围',
    status       VARCHAR(20)     NOT NULL DEFAULT '已发布' COMMENT '状态',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_publisher_id (publisher_id),
    KEY idx_type (type),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 25. 补考表
CREATE TABLE makeup_exam (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    score_entry_id BIGINT UNSIGNED NOT NULL                 COMMENT '关联成绩ID',
    exam_date      DATETIME        NOT NULL                 COMMENT '补考时间',
    location       VARCHAR(100)    NOT NULL                 COMMENT '补考地点',
    status         VARCHAR(20)     NOT NULL DEFAULT '待安排' COMMENT '状态',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_score_entry_id (score_entry_id),
    KEY idx_exam_date (exam_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='补考表';

-- 26. 成绩审计日志表
CREATE TABLE score_audit_log (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    score_entry_id BIGINT UNSIGNED NOT NULL                 COMMENT '成绩ID',
    operator_id    BIGINT UNSIGNED NOT NULL                 COMMENT '操作人ID',
    operator_type  VARCHAR(20)     NOT NULL                 COMMENT '操作人类型：TEACHER/ADMIN',
    operation      VARCHAR(20)     NOT NULL                 COMMENT '操作类型：录入/修改/删除',
    before_data    TEXT            DEFAULT NULL             COMMENT '修改前数据（JSON）',
    after_data     TEXT            DEFAULT NULL             COMMENT '修改后数据（JSON）',
    ip_address     VARCHAR(50)     DEFAULT NULL             COMMENT '操作IP',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_score_entry_id (score_entry_id),
    KEY idx_operator_id (operator_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩审计日志表';

-- ============================================================================
-- 三、外键约束
-- ============================================================================

-- 图书模块外键
ALTER TABLE book ADD CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES book_category(id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_record_user FOREIGN KEY (user_id) REFERENCES student(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_record_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE seat_reservation ADD CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES student(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE seat_reservation ADD CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES seat(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE reading_report ADD CONSTRAINT fk_reading_report_user FOREIGN KEY (user_id) REFERENCES student(id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 教务模块外键
ALTER TABLE major ADD CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE class ADD CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES major(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE class ADD CONSTRAINT fk_class_head_teacher FOREIGN KEY (head_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE teacher ADD CONSTRAINT fk_teacher_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE student ADD CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE admin ADD CONSTRAINT fk_admin_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE course_schedule ADD CONSTRAINT fk_cs_semester FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_schedule ADD CONSTRAINT fk_cs_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_schedule ADD CONSTRAINT fk_cs_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_schedule ADD CONSTRAINT fk_cs_classroom FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_schedule_class ADD CONSTRAINT fk_csc_schedule FOREIGN KEY (schedule_id) REFERENCES course_schedule(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE course_schedule_class ADD CONSTRAINT fk_csc_class FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE course_selection_period ADD CONSTRAINT fk_csp_semester FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_selection ADD CONSTRAINT fk_cs2_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_selection ADD CONSTRAINT fk_cs2_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_selection ADD CONSTRAINT fk_cs2_semester FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE score_entry ADD CONSTRAINT fk_se_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE score_entry ADD CONSTRAINT fk_se_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE score_entry ADD CONSTRAINT fk_se_semester FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE score_entry ADD CONSTRAINT fk_se_makeup_exam FOREIGN KEY (makeup_exam_id) REFERENCES makeup_exam(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE leave_request ADD CONSTRAINT fk_lr_applicant_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE leave_approval_log ADD CONSTRAINT fk_lal_leave_request FOREIGN KEY (leave_request_id) REFERENCES leave_request(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE leave_approval_log ADD CONSTRAINT fk_lal_approver FOREIGN KEY (approver_id) REFERENCES teacher(id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE course_reschedule ADD CONSTRAINT fk_cr_leave_request FOREIGN KEY (leave_request_id) REFERENCES leave_request(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE course_reschedule ADD CONSTRAINT fk_cr_course_schedule FOREIGN KEY (course_schedule_id) REFERENCES course_schedule(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE course_reschedule ADD CONSTRAINT fk_cr_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE notice ADD CONSTRAINT fk_notice_publisher_id FOREIGN KEY (publisher_id) REFERENCES teacher(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE makeup_exam ADD CONSTRAINT fk_makeup_exam_score_entry_id FOREIGN KEY (score_entry_id) REFERENCES score_entry(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE score_audit_log ADD CONSTRAINT fk_audit_score_entry FOREIGN KEY (score_entry_id) REFERENCES score_entry(id) ON DELETE CASCADE ON UPDATE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;