-- ============================================================================
-- 智慧校园微服务系统 数据库设计 v3.0
-- 架构模式: 微服务（Spring Cloud + Spring Boot 3）
-- 数据库: MySQL 8.0（按服务分库，当前阶段共享实例）
-- 字符集: utf8mb4 / 排序规则: utf8mb4_unicode_ci
-- 引擎: InnoDB
-- 规范依据: 《阿里巴巴Java开发手册》
-- ============================================================================

-- ############################################################################
-- ##  数据库 1: smart_system（system-service）
-- ##  范围: 组织架构、教室、通知、系统配置
-- ############################################################################
CREATE DATABASE IF NOT EXISTS smart_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_system;

-- 1.1 学院表
CREATE TABLE IF NOT EXISTS college (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    college_code    VARCHAR(32)       NOT NULL                 COMMENT '学院编码（唯一）',
    college_name    VARCHAR(64)       NOT NULL                 COMMENT '学院名称',
    dean            VARCHAR(32)       DEFAULT NULL             COMMENT '院长姓名',
    contact_phone   VARCHAR(20)       DEFAULT NULL             COMMENT '联系电话',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / INACTIVE',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_college_code (college_code),
    KEY idx_college_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院表';

-- 1.2 专业表
CREATE TABLE IF NOT EXISTS major (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    college_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '所属学院ID',
    major_code      VARCHAR(32)       NOT NULL                 COMMENT '专业编码（唯一）',
    major_name      VARCHAR(64)       NOT NULL                 COMMENT '专业名称',
    study_years     TINYINT UNSIGNED  NOT NULL DEFAULT 4       COMMENT '学制（年）',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / INACTIVE',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_major_code (major_code),
    KEY idx_major_college (college_id),
    KEY idx_major_status (status),
    CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES college (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业表';

-- 1.3 班级表
CREATE TABLE IF NOT EXISTS class (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    major_id        BIGINT UNSIGNED   NOT NULL                 COMMENT '所属专业ID',
    class_name      VARCHAR(64)       NOT NULL                 COMMENT '班级名称',
    head_teacher_id BIGINT UNSIGNED   DEFAULT NULL             COMMENT '班主任教师ID',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / GRADUATED / INACTIVE',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_class_major (major_id),
    KEY idx_class_status (status),
    CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES major (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 1.4 学期表
CREATE TABLE IF NOT EXISTS semester (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    name            VARCHAR(64)       NOT NULL                 COMMENT '学期名称（如 2025-2026-1）',
    start_date      DATE              NOT NULL                 COMMENT '学期开始日期',
    end_date        DATE              NOT NULL                 COMMENT '学期结束日期',
    is_current      TINYINT(1)        NOT NULL DEFAULT 0       COMMENT '是否当前学期: 0=否 1=是',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / COMPLETED / INACTIVE',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_semester_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期表';

-- 1.5 教室表
CREATE TABLE IF NOT EXISTS classroom (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    building        VARCHAR(64)       NOT NULL                 COMMENT '教学楼',
    room_number     VARCHAR(32)       NOT NULL                 COMMENT '房间号',
    capacity        INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '容量（座位数）',
    type            VARCHAR(32)       NOT NULL DEFAULT 'CLASSROOM' COMMENT '类型: CLASSROOM / LAB / LECTURE_HALL / STUDY_ROOM',
    has_projector   TINYINT(1)        NOT NULL DEFAULT 0       COMMENT '是否有投影: 0=否 1=是',
    status          VARCHAR(16)       NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE / MAINTENANCE / DISABLED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_classroom_building_room (building, room_number),
    KEY idx_classroom_status (status),
    KEY idx_classroom_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室表';

-- 1.6 通知表
CREATE TABLE IF NOT EXISTS notice (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    title           VARCHAR(128)      NOT NULL                 COMMENT '通知标题',
    content         TEXT              NOT NULL                 COMMENT '通知内容',
    publisher_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '发布人ID',
    publisher_name  VARCHAR(32)       NOT NULL                 COMMENT '发布人姓名',
    publish_time    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    target_type     VARCHAR(32)       NOT NULL DEFAULT 'ALL'   COMMENT '目标类型: ALL / COLLEGE / MAJOR / CLASS / STUDENT',
    target_id       BIGINT UNSIGNED   DEFAULT NULL             COMMENT '目标ID',
    target_name     VARCHAR(64)       DEFAULT NULL             COMMENT '目标名称（冗余）',
    status          VARCHAR(16)       NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED / REVOKED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_notice_publisher (publisher_id),
    KEY idx_notice_status (status),
    KEY idx_notice_target (target_type, target_id),
    KEY idx_notice_publish_time (publish_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 1.7 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    config_key      VARCHAR(64)       NOT NULL                 COMMENT '配置键（唯一）',
    config_value    VARCHAR(512)      NOT NULL                 COMMENT '配置值',
    description     VARCHAR(256)      DEFAULT NULL             COMMENT '配置描述',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';


-- ############################################################################
-- ##  数据库 2: smart_auth（auth-service）
-- ############################################################################
CREATE DATABASE IF NOT EXISTS smart_auth
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_auth;

-- 2.1 统一认证表
CREATE TABLE IF NOT EXISTS user_auth (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    username        VARCHAR(32)       NOT NULL                 COMMENT '登录账号（学号/工号/管理员账号）',
    password        VARCHAR(128)      NOT NULL                 COMMENT '登录密码（BCrypt加密）',
    user_type       VARCHAR(16)       NOT NULL                 COMMENT '用户类型: STUDENT / TEACHER / ADMIN',
    user_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '关联用户ID',
    account_status  VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态: ACTIVE / LOCKED / DISABLED',
    last_login_time DATETIME          DEFAULT NULL             COMMENT '最后登录时间',
    last_login_ip   VARCHAR(45)       DEFAULT NULL             COMMENT '最后登录IP',
    login_fail_count TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '连续登录失败次数',
    lock_until      DATETIME          DEFAULT NULL             COMMENT '锁定截止时间',
    refresh_token   VARCHAR(256)      DEFAULT NULL             COMMENT 'Refresh Token',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_auth_username (username),
    UNIQUE KEY uk_user_auth_user (user_type, user_id),
    KEY idx_user_auth_type (user_type),
    KEY idx_user_auth_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一认证表';

-- 2.2 学生信息表
CREATE TABLE IF NOT EXISTS student (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_no      VARCHAR(32)       NOT NULL                 COMMENT '学号（唯一）',
    name            VARCHAR(32)       NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(8)        NOT NULL                 COMMENT '性别: MALE / FEMALE',
    class_id        BIGINT UNSIGNED   NOT NULL                 COMMENT '班级ID',
    enrollment_date DATE              DEFAULT NULL             COMMENT '入学日期',
    id_card         VARCHAR(18)       DEFAULT NULL             COMMENT '身份证号',
    phone           VARCHAR(20)       DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(64)       DEFAULT NULL             COMMENT '邮箱',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ENROLLED' COMMENT '状态: ENROLLED / GRADUATED / SUSPENDED / DROPPED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_student_class (class_id),
    KEY idx_student_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表';

-- 2.3 教师信息表
CREATE TABLE IF NOT EXISTS teacher (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    teacher_no      VARCHAR(32)       NOT NULL                 COMMENT '工号（唯一）',
    name            VARCHAR(32)       NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(8)        NOT NULL                 COMMENT '性别: MALE / FEMALE',
    college_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '所属学院ID',
    title           VARCHAR(32)       DEFAULT NULL             COMMENT '职称: PROFESSOR / ASSOCIATE_PROFESSOR / LECTURER / ASSISTANT',
    phone           VARCHAR(20)       DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(64)       DEFAULT NULL             COMMENT '邮箱',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / LEAVE / RESIGNED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no),
    KEY idx_teacher_college (college_id),
    KEY idx_teacher_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师信息表';

-- 2.4 管理员信息表
CREATE TABLE IF NOT EXISTS admin (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    admin_no        VARCHAR(32)       NOT NULL                 COMMENT '管理员编号（唯一）',
    name            VARCHAR(32)       NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(8)        NOT NULL                 COMMENT '性别: MALE / FEMALE',
    phone           VARCHAR(20)       DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(64)       DEFAULT NULL             COMMENT '邮箱',
    role            VARCHAR(16)       NOT NULL                 COMMENT '角色: SUPER_ADMIN / ADMIN',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / DISABLED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_no (admin_no),
    KEY idx_admin_role (role),
    KEY idx_admin_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员信息表';


-- ############################################################################
-- ##  数据库 3: smart_teaching（teaching-service）
-- ############################################################################
CREATE DATABASE IF NOT EXISTS smart_teaching
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_teaching;

-- 3.1 课程表
CREATE TABLE IF NOT EXISTS course (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    course_code     VARCHAR(32)       NOT NULL                 COMMENT '课程代码（唯一）',
    course_name     VARCHAR(128)      NOT NULL                 COMMENT '课程名称',
    credit          DECIMAL(4,1)      NOT NULL DEFAULT 0.0     COMMENT '学分',
    hours           INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '总学时',
    type            VARCHAR(16)       NOT NULL DEFAULT 'REQUIRED' COMMENT '课程类型: REQUIRED / ELECTIVE / PUBLIC',
    capacity        INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '课程容量（选修课用，0=不限）',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / INACTIVE',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code (course_code),
    KEY idx_course_type (type),
    KEY idx_course_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 3.2 排课表
CREATE TABLE IF NOT EXISTS course_schedule (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    teacher_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '授课教师ID',
    classroom_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '教室ID',
    class_ids       VARCHAR(500)      NOT NULL                 COMMENT '授课班级ID列表（逗号分隔）',
    week_day        TINYINT UNSIGNED  NOT NULL                 COMMENT '星期（1-7）',
    start_section   TINYINT UNSIGNED  NOT NULL                 COMMENT '开始节次',
    end_section     TINYINT UNSIGNED  NOT NULL                 COMMENT '结束节次',
    week_range      VARCHAR(100)      NOT NULL                 COMMENT '周次范围（如 1-16）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_csched_semester (semester_id),
    KEY idx_csched_course (course_id),
    KEY idx_csched_teacher (teacher_id),
    KEY idx_csched_classroom (classroom_id),
    KEY idx_csched_time (week_day, start_section, end_section),
    KEY idx_csched_semester_teacher (semester_id, teacher_id),
    CONSTRAINT fk_csched_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课表';

-- 3.3 选课时段表
CREATE TABLE IF NOT EXISTS course_selection_period (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    start_time      DATETIME          NOT NULL                 COMMENT '选课开始时间',
    end_time        DATETIME          NOT NULL                 COMMENT '选课结束时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING / ACTIVE / CLOSED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_csp_semester (semester_id),
    KEY idx_csp_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课时段表';

-- 3.4 选课记录表
CREATE TABLE IF NOT EXISTS course_selection (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    status          VARCHAR(16)       NOT NULL DEFAULT 'SELECTED' COMMENT '状态: SELECTED / DROPPED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '选课时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_csel_student_course_semester (student_id, course_id, semester_id),
    KEY idx_csel_semester (semester_id),
    KEY idx_csel_course (course_id),
    KEY idx_csel_student (student_id),
    KEY idx_csel_status (status),
    CONSTRAINT fk_csel_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课记录表';

-- 3.5 成绩表
CREATE TABLE IF NOT EXISTS score_entry (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    usual_score     DECIMAL(5,1)      DEFAULT NULL             COMMENT '平时成绩（0-100）',
    final_score     DECIMAL(5,1)      DEFAULT NULL             COMMENT '期末成绩（0-100）',
    total_score     DECIMAL(5,1)      DEFAULT NULL             COMMENT '总评成绩（自动计算）',
    score_point     DECIMAL(3,1)      DEFAULT NULL             COMMENT '绩点（自动换算）',
    exam_status     VARCHAR(32)       NOT NULL DEFAULT 'PENDING' COMMENT '考试状态: PENDING / PASSED / FAILED / ABSENT / RESTART_PASSED / RESTART_FAILED',
    makeup_score    DECIMAL(5,1)      DEFAULT NULL             COMMENT '补考成绩',
    makeup_exam_id  BIGINT UNSIGNED   DEFAULT NULL             COMMENT '关联补考ID',
    entered_by      BIGINT UNSIGNED   DEFAULT NULL             COMMENT '录入人ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_score_student_course_semester (student_id, course_id, semester_id),
    KEY idx_score_course (course_id),
    KEY idx_score_semester (semester_id),
    KEY idx_score_exam_status (exam_status),
    CONSTRAINT fk_score_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- 3.6 补考安排表
CREATE TABLE IF NOT EXISTS makeup_exam (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    score_entry_id  BIGINT UNSIGNED   NOT NULL                 COMMENT '关联成绩ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    exam_date       DATE              NOT NULL                 COMMENT '补考日期',
    start_time      TIME              NOT NULL                 COMMENT '开始时间',
    end_time        TIME              NOT NULL                 COMMENT '结束时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态: SCHEDULED / FINISHED / CANCELLED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_mexam_score (score_entry_id),
    KEY idx_mexam_student (student_id),
    KEY idx_mexam_course (course_id),
    KEY idx_mexam_semester (semester_id),
    KEY idx_mexam_status (status),
    CONSTRAINT fk_mexam_score FOREIGN KEY (score_entry_id) REFERENCES score_entry (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mexam_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='补考安排表';

-- 3.7 重修记录表（v3.0 新增）
CREATE TABLE IF NOT EXISTS retake (
    id                      BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_id              BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    course_id               BIGINT UNSIGNED   NOT NULL                 COMMENT '重修课程ID',
    original_score_entry_id BIGINT UNSIGNED   NOT NULL                 COMMENT '原始成绩记录ID',
    semester_id             BIGINT UNSIGNED   NOT NULL                 COMMENT '重修所在学期ID',
    course_schedule_id      BIGINT UNSIGNED   DEFAULT NULL             COMMENT '重修排课ID（指定跟哪个班上课）',
    retake_reason           VARCHAR(128)      NOT NULL DEFAULT '补考未通过' COMMENT '重修原因',
    retake_score            DECIMAL(5,1)      DEFAULT NULL             COMMENT '重修成绩',
    retake_status           VARCHAR(16)       NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING / PASSED / FAILED',
    create_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_retake_student_course_semester (student_id, course_id, semester_id),
    KEY idx_retake_original_score (original_score_entry_id),
    KEY idx_retake_semester (semester_id),
    KEY idx_retake_course (course_id),
    KEY idx_retake_status (retake_status),
    CONSTRAINT fk_retake_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_retake_original_score FOREIGN KEY (original_score_entry_id) REFERENCES score_entry (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='重修记录表';

-- 3.8 考试表
CREATE TABLE IF NOT EXISTS exam (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    exam_name       VARCHAR(128)      NOT NULL                 COMMENT '考试名称',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    exam_type       VARCHAR(16)       NOT NULL                 COMMENT '考试类型: FINAL / MAKEUP / RETAKE',
    start_time      DATETIME          NOT NULL                 COMMENT '考试开始时间',
    end_time        DATETIME          NOT NULL                 COMMENT '考试结束时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态: SCHEDULED / ONGOING / FINISHED / CANCELLED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_exam_course (course_id),
    KEY idx_exam_semester (semester_id),
    KEY idx_exam_type (exam_type),
    KEY idx_exam_status (status),
    CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- 3.9 考场表
CREATE TABLE IF NOT EXISTS exam_room (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    exam_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '考试ID',
    classroom_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '教室ID',
    allocated_count INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '已分配考生数',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_exam_room (exam_id, classroom_id),
    KEY idx_examroom_classroom (classroom_id),
    CONSTRAINT fk_examroom_exam FOREIGN KEY (exam_id) REFERENCES exam (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考场表';

-- 3.10 考生关联表
CREATE TABLE IF NOT EXISTS exam_student (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    exam_room_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '考场ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    seat_number     VARCHAR(16)       DEFAULT NULL             COMMENT '座位号',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ASSIGNED' COMMENT '状态: ASSIGNED / CHECKED_IN / ABSENT / FINISHED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_examstudent_room_student (exam_room_id, student_id),
    KEY idx_examstudent_student (student_id),
    CONSTRAINT fk_examstudent_examroom FOREIGN KEY (exam_room_id) REFERENCES exam_room (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考生关联表';

-- 3.11 评价窗口表
CREATE TABLE IF NOT EXISTS evaluation_period (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    name            VARCHAR(64)       NOT NULL                 COMMENT '评价窗口名称',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    start_time      DATETIME          NOT NULL                 COMMENT '评价开始时间',
    end_time        DATETIME          NOT NULL                 COMMENT '评价结束时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING / ACTIVE / CLOSED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_eperiod_semester (semester_id),
    KEY idx_eperiod_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价窗口表';

-- 3.12 课程评价表
CREATE TABLE IF NOT EXISTS course_evaluation (
    id                  BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    evaluation_period_id BIGINT UNSIGNED  NOT NULL                 COMMENT '评价窗口ID',
    course_id           BIGINT UNSIGNED   NOT NULL                 COMMENT '被评价课程ID',
    student_id          BIGINT UNSIGNED   NOT NULL                 COMMENT '评价学生ID',
    teacher_id          BIGINT UNSIGNED   NOT NULL                 COMMENT '被评价教师ID',
    teaching_score      TINYINT UNSIGNED  NOT NULL                 COMMENT '教学质量评分（1-5）',
    method_score        TINYINT UNSIGNED  NOT NULL                 COMMENT '教学方法评分（1-5）',
    atmosphere_score    TINYINT UNSIGNED  NOT NULL                 COMMENT '课堂氛围评分（1-5）',
    assessment_score    TINYINT UNSIGNED  NOT NULL                 COMMENT '考核公平性评分（1-5）',
    comment             VARCHAR(500)      DEFAULT NULL             COMMENT '文字评价',
    is_anonymous        TINYINT(1)        NOT NULL DEFAULT 0       COMMENT '是否匿名: 0=否 1=是',
    create_time         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '评价时间',
    update_time         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ceval_student_course_period (student_id, course_id, evaluation_period_id),
    KEY idx_ceval_period (evaluation_period_id),
    KEY idx_ceval_course (course_id),
    KEY idx_ceval_teacher (teacher_id),
    CONSTRAINT fk_ceval_period FOREIGN KEY (evaluation_period_id) REFERENCES evaluation_period (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ceval_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程评价表';

-- 3.13 考勤任务表
CREATE TABLE IF NOT EXISTS attendance_task (
    id                  BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    course_schedule_id  BIGINT UNSIGNED   NOT NULL                 COMMENT '排课ID',
    teacher_id          BIGINT UNSIGNED   NOT NULL                 COMMENT '发起教师ID',
    check_type          VARCHAR(16)       NOT NULL                 COMMENT '考勤类型: CHECK_IN / CHECK_OUT / BOTH',
    start_time          DATETIME          NOT NULL                 COMMENT '签到开始时间',
    end_time            DATETIME          NOT NULL                 COMMENT '签到截止时间',
    late_minutes        INT UNSIGNED      NOT NULL DEFAULT 15      COMMENT '迟到阈值（分钟）',
    status              VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / FINISHED',
    create_time         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_attask_schedule (course_schedule_id),
    KEY idx_attask_teacher (teacher_id),
    KEY idx_attask_status (status),
    CONSTRAINT fk_attask_schedule FOREIGN KEY (course_schedule_id) REFERENCES course_schedule (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤任务表';

-- 3.14 考勤记录表
CREATE TABLE IF NOT EXISTS attendance (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    task_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '考勤任务ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    check_in_time   DATETIME          DEFAULT NULL             COMMENT '签到时间',
    check_out_time  DATETIME          DEFAULT NULL             COMMENT '签退时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ABSENT' COMMENT '状态: ABSENT / CHECKED_IN / LATE / CHECKED_OUT / LEAVE',
    longitude       DECIMAL(10,7)     DEFAULT NULL             COMMENT '经度',
    latitude        DECIMAL(10,7)     DEFAULT NULL             COMMENT '纬度',
    device_info     VARCHAR(128)      DEFAULT NULL             COMMENT '设备信息',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_att_task_student (task_id, student_id),
    KEY idx_att_status (status),
    CONSTRAINT fk_att_task FOREIGN KEY (task_id) REFERENCES attendance_task (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- 3.15 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    user_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '操作用户ID',
    user_type       VARCHAR(16)       NOT NULL                 COMMENT '用户类型: STUDENT / TEACHER / ADMIN',
    user_name       VARCHAR(32)       NOT NULL                 COMMENT '用户姓名',
    module          VARCHAR(64)       NOT NULL                 COMMENT '操作模块',
    action          VARCHAR(64)       NOT NULL                 COMMENT '操作动作',
    target_type     VARCHAR(64)       DEFAULT NULL             COMMENT '操作目标类型',
    target_id       BIGINT UNSIGNED   DEFAULT NULL             COMMENT '操作目标ID',
    request_method  VARCHAR(10)       DEFAULT NULL             COMMENT '请求方法',
    request_url     VARCHAR(256)      DEFAULT NULL             COMMENT '请求URL',
    request_params  TEXT              DEFAULT NULL             COMMENT '请求参数',
    response_code   INT               DEFAULT NULL             COMMENT '响应状态码',
    ip_address      VARCHAR(45)       DEFAULT NULL             COMMENT '客户端IP',
    user_agent      VARCHAR(256)      DEFAULT NULL             COMMENT 'User-Agent',
    cost_time       BIGINT UNSIGNED   DEFAULT NULL             COMMENT '耗时（毫秒）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_olog_user (user_id, user_type),
    KEY idx_olog_module (module),
    KEY idx_olog_action (action),
    KEY idx_olog_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';


-- ############################################################################
-- ##  数据库 4: smart_leave（leave-service）
-- ############################################################################
CREATE DATABASE IF NOT EXISTS smart_leave
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_leave;

-- 4.1 请假/调课申请表
CREATE TABLE IF NOT EXISTS leave_request (
    id                  BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    applicant_type      VARCHAR(16)       NOT NULL                 COMMENT '申请人类型: STUDENT / TEACHER',
    applicant_id        BIGINT UNSIGNED   NOT NULL                 COMMENT '申请人ID',
    applicant_name      VARCHAR(32)       NOT NULL                 COMMENT '申请人姓名（冗余）',
    leave_type          VARCHAR(16)       NOT NULL                 COMMENT '请假类型: SICK / PERSONAL / OFFICIAL / RESCHEDULE',
    start_time          DATETIME          NOT NULL                 COMMENT '请假/调课开始时间',
    end_time            DATETIME          NOT NULL                 COMMENT '请假/调课结束时间',
    reason              VARCHAR(500)      NOT NULL                 COMMENT '请假/调课事由',
    course_schedule_id  BIGINT UNSIGNED   DEFAULT NULL             COMMENT '教师调课关联排课ID',
    new_date            DATE              DEFAULT NULL             COMMENT '调课后新日期',
    new_week_day        TINYINT UNSIGNED  DEFAULT NULL             COMMENT '调课后新星期（1-7）',
    new_start_section   TINYINT UNSIGNED  DEFAULT NULL             COMMENT '调课后新开始节次',
    new_end_section     TINYINT UNSIGNED  DEFAULT NULL             COMMENT '调课后新结束节次',
    new_classroom_id    BIGINT UNSIGNED   DEFAULT NULL             COMMENT '调课后新教室ID',
    status              VARCHAR(16)       NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING / APPROVED / REJECTED / CANCELLED',
    create_time         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '申请时间',
    update_time         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_lr_applicant (applicant_type, applicant_id),
    KEY idx_lr_type (leave_type),
    KEY idx_lr_status (status),
    KEY idx_lr_time (start_time, end_time),
    KEY idx_lr_schedule (course_schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假/调课申请表';

-- 4.2 审批日志表
CREATE TABLE IF NOT EXISTS leave_approval_log (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    leave_request_id BIGINT UNSIGNED  NOT NULL                 COMMENT '请假/调课申请ID',
    approver_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '审批人ID',
    approver_name   VARCHAR(32)       NOT NULL                 COMMENT '审批人姓名',
    approver_type   VARCHAR(16)       NOT NULL                 COMMENT '审批人类型: TEACHER / ADMIN',
    result          VARCHAR(16)       NOT NULL                 COMMENT '审批结果: APPROVED / REJECTED',
    comment         VARCHAR(500)      DEFAULT NULL             COMMENT '审批意见',
    approve_time    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '审批时间',
    PRIMARY KEY (id),
    KEY idx_lal_request (leave_request_id),
    KEY idx_lal_approver (approver_id),
    CONSTRAINT fk_lal_request FOREIGN KEY (leave_request_id) REFERENCES leave_request (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批日志表';


-- ############################################################################
-- ##  数据库 5: smart_library（library-service）
-- ############################################################################
CREATE DATABASE IF NOT EXISTS smart_library
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_library;

-- 5.1 图书分类表
CREATE TABLE IF NOT EXISTS book_category (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    category_name   VARCHAR(64)       NOT NULL                 COMMENT '分类名称',
    category_code   VARCHAR(32)       NOT NULL                 COMMENT '分类编码（唯一）',
    parent_id       BIGINT UNSIGNED   DEFAULT NULL             COMMENT '父分类ID',
    sort_order      INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '排序顺序',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_bcat_code (category_code),
    KEY idx_bcat_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- 5.2 图书表
CREATE TABLE IF NOT EXISTS book (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    isbn            VARCHAR(20)       NOT NULL                 COMMENT 'ISBN编号',
    title           VARCHAR(128)      NOT NULL                 COMMENT '书名',
    author          VARCHAR(128)      NOT NULL                 COMMENT '作者',
    publisher       VARCHAR(64)       DEFAULT NULL             COMMENT '出版社',
    publish_date    DATE              DEFAULT NULL             COMMENT '出版日期',
    category_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '图书分类ID',
    total_copies    INT UNSIGNED      NOT NULL DEFAULT 1       COMMENT '馆藏总数',
    available_copies INT UNSIGNED     NOT NULL DEFAULT 1       COMMENT '可借阅数',
    cover_image     VARCHAR(256)      DEFAULT NULL             COMMENT '封面图片URL',
    description     TEXT              DEFAULT NULL             COMMENT '图书简介',
    status          VARCHAR(16)       NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE / OFF_SHELF',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_isbn (isbn),
    KEY idx_book_category (category_id),
    KEY idx_book_title (title),
    KEY idx_book_author (author),
    KEY idx_book_status (status),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES book_category (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- 5.3 借阅记录表
CREATE TABLE IF NOT EXISTS borrow_record (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    borrow_no       VARCHAR(32)       NOT NULL                 COMMENT '借阅编号（唯一）',
    book_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '图书ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '借阅学生ID',
    borrow_date     DATE              NOT NULL                 COMMENT '借阅日期',
    due_date        DATE              NOT NULL                 COMMENT '应还日期',
    return_date     DATE              DEFAULT NULL             COMMENT '实际归还日期',
    status          VARCHAR(16)       NOT NULL DEFAULT 'BORROWED' COMMENT '状态: BORROWED / RETURNED / OVERDUE',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_borrow_no (borrow_no),
    KEY idx_borrow_book (book_id),
    KEY idx_borrow_student (student_id),
    KEY idx_borrow_status (status),
    KEY idx_borrow_due_date (due_date),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 5.4 阅读报告表
CREATE TABLE IF NOT EXISTS reading_report (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    total_borrows   INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '总借阅次数',
    total_days      INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '总阅读天数',
    longest_book    VARCHAR(128)      DEFAULT NULL             COMMENT '借阅最久的书名',
    favorite_category VARCHAR(64)     DEFAULT NULL             COMMENT '最喜爱分类名称',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_rpt_student_semester (student_id, semester_id),
    KEY idx_rpt_semester (semester_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读报告表';


-- ############################################################################
-- ##  数据库 6: smart_seat（seat-service）
-- ############################################################################
CREATE DATABASE IF NOT EXISTS smart_seat
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_seat;

-- 6.1 座位表
CREATE TABLE IF NOT EXISTS seat (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    classroom_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '所属教室ID',
    seat_number     VARCHAR(16)       NOT NULL                 COMMENT '座位编号',
    status          VARCHAR(16)       NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE / MAINTENANCE / DISABLED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_seat_room_number (classroom_id, seat_number),
    KEY idx_seat_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- 6.2 座位预约表
CREATE TABLE IF NOT EXISTS seat_reservation (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    seat_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '座位ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '预约学生ID',
    reserve_date    DATE              NOT NULL                 COMMENT '预约日期',
    start_time      TIME              NOT NULL                 COMMENT '预约开始时间',
    end_time        TIME              NOT NULL                 COMMENT '预约结束时间',
    check_in_time   DATETIME          DEFAULT NULL             COMMENT '签到时间',
    check_out_time  DATETIME          DEFAULT NULL             COMMENT '签退时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'RESERVED' COMMENT '状态: RESERVED / CHECKED_IN / CHECKED_OUT / CANCELLED / EXPIRED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_sres_seat (seat_id),
    KEY idx_sres_student (student_id),
    KEY idx_sres_date (reserve_date),
    KEY idx_sres_status (status),
    KEY idx_sres_seat_date (seat_id, reserve_date),
    CONSTRAINT fk_sres_seat FOREIGN KEY (seat_id) REFERENCES seat (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位预约表';


-- ############################################################################
-- ##  数据库 7: smart_activity（activity-service）
-- ############################################################################
CREATE DATABASE IF NOT EXISTS smart_activity
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_activity;

-- 7.1 社团表
CREATE TABLE IF NOT EXISTS club (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    club_name       VARCHAR(64)       NOT NULL                 COMMENT '社团名称（唯一）',
    club_type       VARCHAR(32)       NOT NULL DEFAULT 'OTHER' COMMENT '社团类型: ACADEMIC / SPORTS / ARTS / VOLUNTEER / OTHER',
    description     TEXT              DEFAULT NULL             COMMENT '社团简介',
    logo_url        VARCHAR(256)      DEFAULT NULL             COMMENT '社团Logo URL',
    president_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '社长学生ID',
    advisor_id      BIGINT UNSIGNED   DEFAULT NULL             COMMENT '指导教师ID',
    member_count    INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '成员数量（冗余）',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / DISSOLVED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_club_name (club_name),
    KEY idx_club_type (club_type),
    KEY idx_club_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团表';

-- 7.2 社团成员表
CREATE TABLE IF NOT EXISTS club_member (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    club_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '社团ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    role            VARCHAR(16)       NOT NULL DEFAULT 'MEMBER' COMMENT '角色: PRESIDENT / VICE_PRESIDENT / ORGANIZER / MEMBER',
    join_time       DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '加入时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE / LEFT',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_cm_club_student (club_id, student_id),
    KEY idx_cm_student (student_id),
    CONSTRAINT fk_cm_club FOREIGN KEY (club_id) REFERENCES club (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团成员表';

-- 7.3 校园活动表
CREATE TABLE IF NOT EXISTS campus_event (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    event_name      VARCHAR(128)      NOT NULL                 COMMENT '活动名称',
    club_id         BIGINT UNSIGNED   DEFAULT NULL             COMMENT '主办社团ID',
    event_type      VARCHAR(32)       NOT NULL DEFAULT 'OTHER' COMMENT '活动类型: LECTURE / COMPETITION / VOLUNTEER / PERFORMANCE / OTHER',
    start_time      DATETIME          NOT NULL                 COMMENT '活动开始时间',
    end_time        DATETIME          NOT NULL                 COMMENT '活动结束时间',
    location        VARCHAR(128)      NOT NULL                 COMMENT '活动地点',
    max_participants INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '最大报名人数（0=不限）',
    registered_count INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '已报名人数（冗余）',
    description     TEXT              DEFAULT NULL             COMMENT '活动描述',
    cover_image     VARCHAR(256)      DEFAULT NULL             COMMENT '封面图片URL',
    status          VARCHAR(16)       NOT NULL DEFAULT 'DRAFT'  COMMENT '状态: DRAFT / PUBLISHED / ONGOING / FINISHED / CANCELLED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_cevent_club (club_id),
    KEY idx_cevent_type (event_type),
    KEY idx_cevent_status (status),
    KEY idx_cevent_time (start_time, end_time),
    CONSTRAINT fk_cevent_club FOREIGN KEY (club_id) REFERENCES club (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园活动表';

-- 7.4 活动报名表
CREATE TABLE IF NOT EXISTS event_registration (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    event_id        BIGINT UNSIGNED   NOT NULL                 COMMENT '活动ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '报名学生ID',
    status          VARCHAR(16)       NOT NULL DEFAULT 'REGISTERED' COMMENT '状态: REGISTERED / SIGNED_IN / CANCELLED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '报名时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ereg_event_student (event_id, student_id),
    KEY idx_ereg_student (student_id),
    CONSTRAINT fk_ereg_event FOREIGN KEY (event_id) REFERENCES campus_event (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 7.5 奖学金定义表
CREATE TABLE IF NOT EXISTS scholarship (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    name            VARCHAR(128)      NOT NULL                 COMMENT '奖学金名称',
    type            VARCHAR(16)       NOT NULL                 COMMENT '类型: SCHOLARSHIP / GRANT',
    grade           VARCHAR(16)       NOT NULL                 COMMENT '等级: NATIONAL / PROVINCIAL / SCHOOL / ENTERPRISE',
    amount          DECIMAL(10,2)     NOT NULL                 COMMENT '金额（元）',
    quota           INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '名额（0=不限）',
    conditions      TEXT              DEFAULT NULL             COMMENT '申请条件',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '所属学期ID',
    start_time      DATETIME          NOT NULL                 COMMENT '申请开始时间',
    end_time        DATETIME          NOT NULL                 COMMENT '申请截止时间',
    status          VARCHAR(16)       NOT NULL DEFAULT 'DRAFT'  COMMENT '状态: DRAFT / ACTIVE / CLOSED',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_sch_type (type),
    KEY idx_sch_semester (semester_id),
    KEY idx_sch_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖学金定义表';

-- 7.6 奖学金申请表
CREATE TABLE IF NOT EXISTS scholarship_application (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    scholarship_id  BIGINT UNSIGNED   NOT NULL                 COMMENT '奖学金ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '申请学生ID',
    apply_reason    VARCHAR(1000)     NOT NULL                 COMMENT '申请理由',
    attachment_url  VARCHAR(256)      DEFAULT NULL             COMMENT '证明材料URL',
    status          VARCHAR(16)       NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING / APPROVED / REJECTED',
    review_remark   VARCHAR(500)      DEFAULT NULL             COMMENT '审核备注',
    reviewer_id     BIGINT UNSIGNED   DEFAULT NULL             COMMENT '审核人ID',
    review_time     DATETIME          DEFAULT NULL             COMMENT '审核时间',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '申请时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_schapp_scholarship_student (scholarship_id, student_id),
    KEY idx_schapp_student (student_id),
    KEY idx_schapp_status (status),
    CONSTRAINT fk_schapp_scholarship FOREIGN KEY (scholarship_id) REFERENCES scholarship (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖学金申请表';


-- ============================================================================
-- ##  初始数据
-- ============================================================================

-- -------- smart_system --------
USE smart_system;

INSERT IGNORE INTO sys_config (config_key, config_value, description) VALUES
('borrow_max_count', '5', '学生最大同时借阅册数'),
('borrow_max_days', '30', '最大借阅天数'),
('seat_reserve_open_time', '22:00', '次日座位预约开放时间'),
('usual_score_ratio', '0.3', '平时成绩占比'),
('final_score_ratio', '0.7', '期末成绩占比'),
('makeup_score_limit', '60', '补考成绩上限'),
('login_fail_max_count', '5', '登录失败锁定阈值'),
('login_lock_minutes', '30', '登录锁定分钟数'),
('token_expire_hours', '24', 'JWT Token有效期（小时）'),
('refresh_token_expire_days', '7', 'RefreshToken有效期（天）');

INSERT IGNORE INTO semester (id, name, start_date, end_date, is_current, status) VALUES
(1, '2025-2026-1', '2025-09-01', '2026-01-15', 0, 'COMPLETED'),
(2, '2025-2026-2', '2026-02-16', '2026-06-30', 1, 'ACTIVE');

INSERT IGNORE INTO college (id, college_code, college_name, status) VALUES
(1, 'CS', '计算机科学与技术学院', 'ACTIVE'),
(2, 'MATH', '数学与统计学院', 'ACTIVE'),
(3, 'ENG', '外国语学院', 'ACTIVE');

INSERT IGNORE INTO major (id, college_id, major_code, major_name) VALUES
(1, 1, 'CS-SE', '软件工程'),
(2, 1, 'CS-CS', '计算机科学与技术'),
(3, 2, 'MATH-AM', '应用数学'),
(4, 3, 'ENG-EN', '英语');

INSERT IGNORE INTO class (id, major_id, class_name) VALUES
(1, 1, '软件工程2025-1班'),
(2, 1, '软件工程2025-2班'),
(3, 2, '计算机科学与技术2025-1班');

-- -------- smart_auth --------
USE smart_auth;

INSERT IGNORE INTO admin (id, admin_no, name, gender, role, status) VALUES
(1, 'admin001', '系统管理员', 'MALE', 'SUPER_ADMIN', 'ACTIVE');

INSERT IGNORE INTO teacher (id, teacher_no, name, gender, college_id, title) VALUES
(1, 'T2025001', '张教授', 'MALE', 1, 'PROFESSOR'),
(2, 'T2025002', '李老师', 'FEMALE', 1, 'LECTURER');

INSERT IGNORE INTO student (id, student_no, name, gender, class_id) VALUES
(1, '2025001001', '张三', 'MALE', 1),
(2, '2025001002', '李四', 'FEMALE', 1),
(3, '2025002001', '王五', 'MALE', 2);

-- BCrypt: admin123 -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT IGNORE INTO user_auth (username, password, user_type, user_id, account_status) VALUES
('admin001',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN',  1, 'ACTIVE'),
('T2025001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'TEACHER', 1, 'ACTIVE'),
('T2025002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'TEACHER', 2, 'ACTIVE'),
('2025001001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, 'ACTIVE'),
('2025001002','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 2, 'ACTIVE'),
('2025002001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 3, 'ACTIVE');