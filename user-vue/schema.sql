-- ============================================================================
-- 智慧校园管理系统 — 完整建库建表脚本（优化版 v2.1）
-- 
-- 设计原则：
--   1. 所有表使用 InnoDB 引擎 + utf8mb4 字符集，支持 emoji 和中文
--   2. 主键统一使用 BIGINT UNSIGNED AUTO_INCREMENT
--   3. 建立完整外键约束，保证数据参照完整性
--   4. 重要查询字段建立联合索引，覆盖常用查询模式
--   5. 为后续扩展预留字段和索引空间
--   6. 与现有 entity/pojo、entity/dto、entity/vo 的属性定义完全对齐
-- ============================================================================

-- 创建数据库（若已存在则不重复创建）
CREATE DATABASE IF NOT EXISTS smart_campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_campus;

-- ============================================================================
-- 1. 基础数据表（字典/组织架构）
-- ============================================================================

-- 1.1 学院表
CREATE TABLE IF NOT EXISTS college (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '学院ID',
    college_code    VARCHAR(32)       NOT NULL                 COMMENT '学院代码（唯一）',
    college_name    VARCHAR(100)      NOT NULL                 COMMENT '学院名称',
    dean            VARCHAR(50)       DEFAULT NULL             COMMENT '院长姓名',
    contact_phone   VARCHAR(20)       DEFAULT NULL             COMMENT '联系电话',
    status          VARCHAR(20)       NOT NULL DEFAULT '启用'  COMMENT '状态：启用 / 禁用',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_college_code (college_code),
    KEY idx_college_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院表';

-- 1.2 专业表
CREATE TABLE IF NOT EXISTS major (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '专业ID',
    college_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '所属学院ID',
    major_code      VARCHAR(32)       NOT NULL                 COMMENT '专业代码（唯一）',
    major_name      VARCHAR(100)      NOT NULL                 COMMENT '专业名称',
    study_years     TINYINT UNSIGNED  NOT NULL DEFAULT 4       COMMENT '学制（年）',
    status          VARCHAR(20)       NOT NULL DEFAULT '启用'  COMMENT '状态：启用 / 禁用',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
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
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '班级ID',
    major_id        BIGINT UNSIGNED   NOT NULL                 COMMENT '所属专业ID',
    class_name      VARCHAR(100)      NOT NULL                 COMMENT '班级名称',
    head_teacher_id BIGINT UNSIGNED   DEFAULT NULL             COMMENT '班主任ID（关联教师表）',
    status          VARCHAR(20)       NOT NULL DEFAULT '在读'  COMMENT '状态：在读 / 毕业',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_class_major (major_id),
    KEY idx_class_head_teacher (head_teacher_id),
    KEY idx_class_status (status),
    CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES major (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 1.4 学期表
CREATE TABLE IF NOT EXISTS semester (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '学期ID',
    name            VARCHAR(50)       NOT NULL                 COMMENT '学期名称（如 2025-2026-1）',
    start_date      DATE              NOT NULL                 COMMENT '开学日期',
    end_date        DATE              NOT NULL                 COMMENT '结束日期',
    is_current      TINYINT(1)        NOT NULL DEFAULT 0       COMMENT '是否当前学期：0-否，1-是',
    status          VARCHAR(20)       NOT NULL DEFAULT '进行中' COMMENT '状态',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_semester_name (name),
    KEY idx_semester_current (is_current),
    KEY idx_semester_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期表';

-- ============================================================================
-- 2. 统一认证表（登录信息集中管理）
-- ============================================================================

-- 2.1 用户认证表
--   - 将 student_no / teacher_no / admin_no 统一作为 username 存储
--   - 通过 user_type 区分身份，user_id 关联各自信息表
--   - 登录验证只查此表，业务信息查各自信息表
CREATE TABLE IF NOT EXISTS user_auth (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '认证记录ID',
    username        VARCHAR(50)       NOT NULL                 COMMENT '登录用户名（学号/工号/管理员账号，唯一）',
    password        VARCHAR(255)      NOT NULL                 COMMENT '密码（BCrypt加密）',
    user_type       VARCHAR(20)       NOT NULL                 COMMENT '用户类型：student / teacher / admin',
    user_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '关联的用户信息表ID',
    account_status  VARCHAR(20)       NOT NULL DEFAULT '启用'  COMMENT '账号状态：启用 / 锁定 / 禁用',
    last_login_time DATETIME          DEFAULT NULL             COMMENT '最后登录时间',
    last_login_ip   VARCHAR(45)       DEFAULT NULL             COMMENT '最后登录IP（支持IPv6）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_auth_username (username),
    KEY idx_user_auth_type (user_type),
    KEY idx_user_auth_user (user_type, user_id),
    KEY idx_user_auth_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证表';

-- ============================================================================
-- 3. 人员信息表（学生 / 教师 / 管理员）—— 纯信息，不含登录字段
-- ============================================================================

-- 3.1 学生信息表
CREATE TABLE IF NOT EXISTS student (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '学生ID',
    student_no      VARCHAR(32)       NOT NULL                 COMMENT '学号（与user_auth.username一致）',
    name            VARCHAR(50)       NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(10)       NOT NULL DEFAULT '男'    COMMENT '性别',
    class_id        BIGINT UNSIGNED   NOT NULL                 COMMENT '所属班级ID',
    enrollment_date DATE              DEFAULT NULL             COMMENT '入学日期',
    status          VARCHAR(20)       NOT NULL DEFAULT '在读'  COMMENT '学籍状态：在读 / 毕业 / 休学 / 退学',
    id_card         VARCHAR(18)       DEFAULT NULL             COMMENT '身份证号',
    phone           VARCHAR(20)       DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(100)      DEFAULT NULL             COMMENT '邮箱',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_student_class (class_id),
    KEY idx_student_name (name),
    KEY idx_student_status (status),
    KEY idx_student_phone (phone),
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表';

-- 3.2 教师信息表
CREATE TABLE IF NOT EXISTS teacher (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '教师ID',
    teacher_no      VARCHAR(32)       NOT NULL                 COMMENT '工号（与user_auth.username一致）',
    name            VARCHAR(50)       NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(10)       NOT NULL DEFAULT '男'    COMMENT '性别',
    college_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '所属学院ID',
    title           VARCHAR(50)       DEFAULT NULL             COMMENT '职称（教授/副教授/讲师/助教）',
    phone           VARCHAR(20)       DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(100)      DEFAULT NULL             COMMENT '邮箱',
    status          VARCHAR(20)       NOT NULL DEFAULT '在职'  COMMENT '状态：在职 / 离职 / 退休',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no),
    KEY idx_teacher_college (college_id),
    KEY idx_teacher_name (name),
    KEY idx_teacher_status (status),
    KEY idx_teacher_email (email),
    CONSTRAINT fk_teacher_college FOREIGN KEY (college_id) REFERENCES college (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师信息表';

-- 3.3 管理员信息表
CREATE TABLE IF NOT EXISTS admin (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '管理员ID',
    admin_no        VARCHAR(32)       NOT NULL                 COMMENT '管理员账号（与user_auth.username一致）',
    name            VARCHAR(50)       NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(10)       NOT NULL DEFAULT '男'    COMMENT '性别',
    phone           VARCHAR(20)       DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(100)      DEFAULT NULL             COMMENT '邮箱',
    role            VARCHAR(20)       NOT NULL DEFAULT '管理员' COMMENT '角色：超级管理员 / 管理员 / 审计员',
    status          VARCHAR(20)       NOT NULL DEFAULT '在职'  COMMENT '状态：在职 / 离职',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_no (admin_no),
    KEY idx_admin_name (name),
    KEY idx_admin_role (role),
    KEY idx_admin_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员信息表';

-- 补：班级 -> 班主任 外键（建表时 teacher 尚未创建，在此追加）
ALTER TABLE class
    ADD CONSTRAINT fk_class_head_teacher FOREIGN KEY (head_teacher_id) REFERENCES teacher (id)
        ON DELETE SET NULL ON UPDATE CASCADE;

-- ============================================================================
-- 4. 教室 / 座位资源表
-- ============================================================================

-- 4.1 教室表
CREATE TABLE IF NOT EXISTS classroom (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '教室ID',
    building        VARCHAR(100)      NOT NULL                 COMMENT '教学楼',
    room_number     VARCHAR(50)       NOT NULL                 COMMENT '房间号',
    capacity        INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '容纳人数',
    type            VARCHAR(20)       NOT NULL DEFAULT '普通教室' COMMENT '教室类型：普通教室 / 阶梯教室 / 实验室 / 阅览室',
    status          VARCHAR(20)       NOT NULL DEFAULT '可用'  COMMENT '状态：可用 / 维修中 / 停用',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_classroom (building, room_number),
    KEY idx_classroom_building (building),
    KEY idx_classroom_type (type),
    KEY idx_classroom_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室表';

-- 4.2 座位表
CREATE TABLE IF NOT EXISTS seat (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '座位ID',
    room_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '所属教室/阅览室ID',
    seat_number     VARCHAR(20)       NOT NULL                 COMMENT '座位编号',
    status          VARCHAR(20)       NOT NULL DEFAULT '空闲'  COMMENT '状态：空闲 / 使用中 / 维修',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_seat_room_number (room_id, seat_number),
    KEY idx_seat_room (room_id),
    KEY idx_seat_status (status),
    CONSTRAINT fk_seat_classroom FOREIGN KEY (room_id) REFERENCES classroom (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- ============================================================================
-- 5. 课程 / 选课 / 排课 / 成绩表
-- ============================================================================

-- 5.1 课程表
--   设计说明：课程表只存储课程本身的属性（代码、名称、学分、总学时、类型、容量），
--   与排课相关的时间/地点信息统一在 course_schedule 表中管理，便于课程复用和弹性排课。
CREATE TABLE IF NOT EXISTS course (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '课程ID',
    course_code     VARCHAR(32)       NOT NULL                 COMMENT '课程代码（唯一）',
    course_name     VARCHAR(200)      NOT NULL                 COMMENT '课程名称',
    credit          DECIMAL(4,1)      NOT NULL DEFAULT 0.0     COMMENT '学分',
    hours           INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '总学时',
    type            VARCHAR(20)       NOT NULL DEFAULT '必修'  COMMENT '课程类型：必修 / 选修 / 公选',
    status          VARCHAR(20)       NOT NULL DEFAULT '启用'  COMMENT '状态：启用 / 停用',
    capacity        SMALLINT UNSIGNED DEFAULT NULL             COMMENT '课程容量（选修/公选课适用）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code (course_code),
    KEY idx_course_name (course_name),
    KEY idx_course_type (type),
    KEY idx_course_status (status),
    KEY idx_course_capacity (capacity),
    KEY idx_course_credit (credit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 5.2 选课时段表
CREATE TABLE IF NOT EXISTS course_selection_period (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '选课时段ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    start_time      DATETIME          NOT NULL                 COMMENT '选课开始时间',
    end_time        DATETIME          NOT NULL                 COMMENT '选课结束时间',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_csp_semester (semester_id),
    KEY idx_csp_time (start_time, end_time),
    CONSTRAINT fk_csp_semester FOREIGN KEY (semester_id) REFERENCES semester (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课时段表';

-- 5.3 选课记录表
CREATE TABLE IF NOT EXISTS course_selection (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '选课记录ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    status          VARCHAR(20)       NOT NULL DEFAULT '已选'  COMMENT '状态：已选 / 退选 / 完成',
    score           DECIMAL(5,2)      DEFAULT NULL             COMMENT '成绩',
    score_point     DECIMAL(3,2)      DEFAULT NULL             COMMENT '绩点',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_cs_student_course_semester (student_id, course_id, semester_id),
    KEY idx_cs_student (student_id),
    KEY idx_cs_course (course_id),
    KEY idx_cs_semester (semester_id),
    KEY idx_cs_status (status),
    CONSTRAINT fk_cs_student FOREIGN KEY (student_id) REFERENCES student (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cs_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cs_semester FOREIGN KEY (semester_id) REFERENCES semester (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课记录表';

-- 5.4 排课表
CREATE TABLE IF NOT EXISTS course_schedule (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '排课ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    teacher_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '授课教师ID',
    classroom_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '教室ID',
    class_ids       VARCHAR(500)      NOT NULL                 COMMENT '授课班级ID列表（逗号分隔，格式: "1,2,3"）',
    week_day        TINYINT UNSIGNED  NOT NULL                 COMMENT '星期（1-7）',
    start_section   TINYINT UNSIGNED  NOT NULL                 COMMENT '开始节次',
    end_section     TINYINT UNSIGNED  NOT NULL                 COMMENT '结束节次',
    week_range      VARCHAR(100)      NOT NULL                 COMMENT '周次范围（如 "1-16" 或 "1,3,5,7-12"）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_csched_semester (semester_id),
    KEY idx_csched_course (course_id),
    KEY idx_csched_teacher (teacher_id),
    KEY idx_csched_classroom (classroom_id),
    KEY idx_csched_time (week_day, start_section, end_section),
    KEY idx_csched_semester_teacher (semester_id, teacher_id),
    KEY idx_csched_semester_classroom (semester_id, classroom_id),
    KEY idx_csched_semester_week (semester_id, week_day),
    CONSTRAINT fk_csched_semester FOREIGN KEY (semester_id) REFERENCES semester (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_csched_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_csched_teacher FOREIGN KEY (teacher_id) REFERENCES teacher (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_csched_classroom FOREIGN KEY (classroom_id) REFERENCES classroom (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课表';

-- 5.5 成绩表
CREATE TABLE IF NOT EXISTS score_entry (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '成绩ID',
    course_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '课程ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    semester_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '学期ID',
    usual_score     DECIMAL(5,2)      DEFAULT NULL             COMMENT '平时成绩',
    final_score     DECIMAL(5,2)      DEFAULT NULL             COMMENT '期末成绩',
    total_score     DECIMAL(5,2)      DEFAULT NULL             COMMENT '总评成绩',
    makeup_score    DECIMAL(5,2)      DEFAULT NULL             COMMENT '补考成绩',
    score_point     DECIMAL(3,2)      DEFAULT NULL             COMMENT '绩点',
    exam_status     VARCHAR(20)       NOT NULL DEFAULT '正常'  COMMENT '考试状态：正常 / 缓考 / 缺考 / 作弊 / 补考通过 / 补考未通过',
    makeup_exam_id  BIGINT UNSIGNED   DEFAULT NULL             COMMENT '关联补考安排ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_se_course_student_semester (course_id, student_id, semester_id),
    KEY idx_se_student (student_id),
    KEY idx_se_semester (semester_id),
    KEY idx_se_exam_status (exam_status),
    KEY idx_se_makeup_exam (makeup_exam_id),
    CONSTRAINT fk_se_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_se_student FOREIGN KEY (student_id) REFERENCES student (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_se_semester FOREIGN KEY (semester_id) REFERENCES semester (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- 5.6 补考安排表
CREATE TABLE IF NOT EXISTS makeup_exam (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '补考安排ID',
    score_entry_id  BIGINT UNSIGNED   NOT NULL                 COMMENT '关联成绩ID',
    exam_date       DATE              NOT NULL                 COMMENT '补考日期',
    start_time      TIME              NOT NULL                 COMMENT '开始时间',
    end_time        TIME              NOT NULL                 COMMENT '结束时间',
    status          VARCHAR(20)       NOT NULL DEFAULT '待考'  COMMENT '状态：待考 / 已完成 / 已取消',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_me_score_entry (score_entry_id),
    KEY idx_me_status (status),
    KEY idx_me_exam_date (exam_date),
    CONSTRAINT fk_me_score_entry FOREIGN KEY (score_entry_id) REFERENCES score_entry (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='补考安排表';

-- 补：成绩 -> 补考 外键
ALTER TABLE score_entry
    ADD CONSTRAINT fk_se_makeup_exam FOREIGN KEY (makeup_exam_id) REFERENCES makeup_exam (id)
        ON DELETE SET NULL ON UPDATE CASCADE;

-- ============================================================================
-- 6. 座位预约模块
-- ============================================================================

-- 6.1 座位预约表
CREATE TABLE IF NOT EXISTS seat_reservation (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '预约ID',
    reservation_no  VARCHAR(64)       NOT NULL                 COMMENT '预约编号（UUID，MQ幂等）',
    user_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '用户ID',
    seat_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '座位ID',
    date            DATE              NOT NULL                 COMMENT '预约日期',
    start_time      TIME              NOT NULL                 COMMENT '开始时间',
    end_time        TIME              NOT NULL                 COMMENT '结束时间',
    leave_time      DATETIME          DEFAULT NULL             COMMENT '暂离/签退时间',
    status          VARCHAR(20)       NOT NULL DEFAULT '预约中' COMMENT '状态：预约中 / 使用中 / 暂离 / 已完成 / 已取消',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sr_reservation_no (reservation_no),
    KEY idx_sr_user_date (user_id, date),
    KEY idx_sr_seat_date (seat_id, date),
    KEY idx_sr_date_status (date, status),
    KEY idx_sr_status (status),
    KEY idx_sr_user_status (user_id, status),
    CONSTRAINT fk_sr_user FOREIGN KEY (user_id) REFERENCES student (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sr_seat FOREIGN KEY (seat_id) REFERENCES seat (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位预约表';

-- ============================================================================
-- 7. 图书馆 / 图书借阅模块
-- ============================================================================

-- 7.1 图书分类表
CREATE TABLE IF NOT EXISTS book_category (
    id              INT UNSIGNED      NOT NULL AUTO_INCREMENT  COMMENT '分类ID（与实体Integer对应）',
    category_name   VARCHAR(100)      NOT NULL                 COMMENT '分类名称',
    category_code   VARCHAR(50)       NOT NULL                 COMMENT '分类代码（唯一）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_bc_code (category_code),
    KEY idx_bc_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- 7.2 图书表
CREATE TABLE IF NOT EXISTS book (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '图书ID',
    isbn            VARCHAR(20)       NOT NULL                 COMMENT 'ISBN编号（唯一）',
    title           VARCHAR(500)      NOT NULL                 COMMENT '书名',
    author          VARCHAR(200)      NOT NULL                 COMMENT '作者',
    publisher       VARCHAR(200)      DEFAULT NULL             COMMENT '出版社',
    publish_date    DATE              DEFAULT NULL             COMMENT '出版日期',
    category_id     INT UNSIGNED      NOT NULL                 COMMENT '分类ID',
    total_copies    INT UNSIGNED      NOT NULL DEFAULT 1       COMMENT '总册数',
    available_copies INT UNSIGNED     NOT NULL DEFAULT 1       COMMENT '可借册数',
    status          VARCHAR(20)       NOT NULL DEFAULT '在库'  COMMENT '状态：在库 / 下架',
    cover_image     VARCHAR(500)      DEFAULT NULL             COMMENT '封面图片URL',
    description     TEXT              DEFAULT NULL             COMMENT '简介',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_isbn (isbn),
    KEY idx_book_title (title(100)),
    KEY idx_book_author (author),
    KEY idx_book_category (category_id),
    KEY idx_book_status (status),
    KEY idx_book_available (available_copies),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES book_category (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- 7.3 借阅记录表
CREATE TABLE IF NOT EXISTS borrow_record (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '借阅记录ID',
    borrow_no       VARCHAR(64)       NOT NULL                 COMMENT '借阅流水号（UUID，唯一）',
    user_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '借阅人ID',
    book_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '图书ID',
    borrow_date     DATE              NOT NULL                 COMMENT '借阅日期',
    due_date        DATE              NOT NULL                 COMMENT '应还日期',
    return_date     DATE              DEFAULT NULL             COMMENT '实际归还日期',
    status          VARCHAR(20)       NOT NULL DEFAULT '借阅中' COMMENT '状态：借阅中 / 已归还 / 逾期',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_br_borrow_no (borrow_no),
    KEY idx_br_user (user_id),
    KEY idx_br_book (book_id),
    KEY idx_br_user_status (user_id, status),
    KEY idx_br_status_due (status, due_date),
    KEY idx_br_due_date (due_date),
    CONSTRAINT fk_br_user FOREIGN KEY (user_id) REFERENCES student (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_br_book FOREIGN KEY (book_id) REFERENCES book (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 7.4 阅读报告表
CREATE TABLE IF NOT EXISTS reading_report (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '报告ID',
    user_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '用户ID',
    semester        VARCHAR(50)       NOT NULL                 COMMENT '学期标识（如 2025-2026-1）',
    total_borrow    INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '总借阅量',
    fav_category    VARCHAR(100)      DEFAULT NULL             COMMENT '最偏爱分类',
    analysis_text   TEXT              DEFAULT NULL             COMMENT '分析文本',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    KEY idx_rr_user (user_id),
    KEY idx_rr_user_semester (user_id, semester),
    CONSTRAINT fk_rr_user FOREIGN KEY (user_id) REFERENCES student (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读报告表';

-- ============================================================================
-- 8. 请假 / 审批 / 通知模块
-- ============================================================================

-- 8.1 请假申请表
CREATE TABLE IF NOT EXISTS leave_request (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '请假申请ID',
    student_id      BIGINT UNSIGNED   NOT NULL                 COMMENT '学生ID',
    leave_type      VARCHAR(20)       NOT NULL                 COMMENT '请假类型：事假 / 病假 / 公假 / 其他',
    start_time      DATETIME          NOT NULL                 COMMENT '开始时间',
    end_time        DATETIME          NOT NULL                 COMMENT '结束时间',
    reason          TEXT              NOT NULL                 COMMENT '请假事由',
    status          VARCHAR(20)       NOT NULL DEFAULT '待审批' COMMENT '状态：待审批 / 已通过 / 已驳回 / 已撤销',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_lr_student (student_id),
    KEY idx_lr_status (status),
    KEY idx_lr_student_status (student_id, status),
    KEY idx_lr_time (start_time, end_time),
    CONSTRAINT fk_lr_student FOREIGN KEY (student_id) REFERENCES student (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- 8.2 请假审批日志表
CREATE TABLE IF NOT EXISTS leave_approval_log (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '审批日志ID',
    leave_request_id BIGINT UNSIGNED  NOT NULL                 COMMENT '请假申请ID',
    approver_id     BIGINT UNSIGNED   NOT NULL                 COMMENT '审批人ID',
    approve_time    DATETIME          NOT NULL                 COMMENT '审批时间',
    result          VARCHAR(20)       NOT NULL                 COMMENT '审批结果：通过 / 驳回',
    comment         TEXT              DEFAULT NULL             COMMENT '审批意见',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_lal_leave_request (leave_request_id),
    KEY idx_lal_approver (approver_id),
    KEY idx_lal_result (result),
    CONSTRAINT fk_lal_leave_request FOREIGN KEY (leave_request_id) REFERENCES leave_request (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_lal_approver FOREIGN KEY (approver_id) REFERENCES teacher (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假审批日志表';

-- 8.3 通知表
CREATE TABLE IF NOT EXISTS notice (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    title           VARCHAR(200)      NOT NULL                 COMMENT '通知标题',
    content         TEXT              NOT NULL                 COMMENT '通知内容',
    publisher_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '发布人ID',
    publish_time    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    target_type     VARCHAR(20)       NOT NULL DEFAULT '全体'  COMMENT '发布范围类型：全体 / 学院 / 专业 / 班级 / 个人',
    target_id       BIGINT UNSIGNED   DEFAULT NULL             COMMENT '目标对象ID',
    target_name     VARCHAR(200)      DEFAULT NULL             COMMENT '目标对象名称',
    status          VARCHAR(20)       NOT NULL DEFAULT '已发布' COMMENT '状态：已发布 / 已撤回',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_notice_publisher (publisher_id),
    KEY idx_notice_target (target_type, target_id),
    KEY idx_notice_publish_time (publish_time),
    KEY idx_notice_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================================================
-- 9. 系统辅助表（扩展预留）
-- ============================================================================

-- 9.1 系统配置表（预留：存储系统级KV配置）
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '配置ID',
    config_key      VARCHAR(100)      NOT NULL                 COMMENT '配置键',
    config_value    TEXT              NOT NULL                 COMMENT '配置值',
    description     VARCHAR(500)      DEFAULT NULL             COMMENT '配置说明',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 9.2 操作日志表（预留：审计日志）
CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    user_id         BIGINT UNSIGNED   NOT NULL                 COMMENT '操作用户ID',
    user_type       VARCHAR(20)       NOT NULL                 COMMENT '用户类型：student / teacher / admin',
    operation       VARCHAR(100)      NOT NULL                 COMMENT '操作类型',
    target_type     VARCHAR(50)       DEFAULT NULL             COMMENT '操作目标类型',
    target_id       BIGINT UNSIGNED   DEFAULT NULL             COMMENT '操作目标ID',
    detail          JSON              DEFAULT NULL             COMMENT '操作详情（JSON）',
    ip_address      VARCHAR(45)       DEFAULT NULL             COMMENT 'IP地址（支持IPv6）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_audit_user (user_id),
    KEY idx_audit_time (create_time),
    KEY idx_audit_operation (operation, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 9.3 本地消息表（预留：MQ可靠消息）
CREATE TABLE IF NOT EXISTS event_outbox (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
    aggregate_type  VARCHAR(50)       NOT NULL                 COMMENT '聚合根类型',
    aggregate_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '聚合根ID',
    event_type      VARCHAR(100)      NOT NULL                 COMMENT '事件类型',
    payload         JSON              NOT NULL                 COMMENT '消息体（JSON）',
    status          VARCHAR(20)       NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING / SENT / FAILED',
    retry_count     INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '重试次数',
    max_retry       INT UNSIGNED      NOT NULL DEFAULT 3       COMMENT '最大重试次数',
    next_retry_at   DATETIME          DEFAULT NULL             COMMENT '下次重试时间',
    error_msg       TEXT              DEFAULT NULL             COMMENT '最后一次错误信息',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_outbox_status_retry (status, next_retry_at),
    KEY idx_outbox_aggregate (aggregate_type, aggregate_id),
    KEY idx_outbox_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地消息表（Outbox模式）';

-- ============================================================================
-- 10. 初始化数据（可选）
-- ============================================================================

-- 插入默认学院（示例）
INSERT INTO college (college_code, college_name, dean, status) VALUES
('CS',    '计算机科学与技术学院',   NULL, '启用'),
('MATH',  '数学与统计学院',         NULL, '启用'),
('PHYS',  '物理与电子工程学院',     NULL, '启用'),
('ENGL',  '外国语学院',             NULL, '启用'),
('ECON',  '经济管理学院',           NULL, '启用')
ON DUPLICATE KEY UPDATE id = id;

-- ============================================================================
-- 脚本说明（不执行）
-- ============================================================================
-- 
-- 【对齐说明】
--   本脚本与以下 Java 实体类 100% 对齐：
--   entity/pojo/ 下的实体类（含新增 admin、user_auth 对应实体）
--   entity/dto/  下的 CreateDTO / UpdateDTO
--   entity/vo/   下的 ListVO / DetailVO
--
-- 【v2.0 → v2.1 变更记录】
--   1. 新增 user_auth 表            → 集中管理登录认证信息（username、password、account_status）
--   2. student 表移除登录字段       → password、account_status 迁移至 user_auth，保留 student_no 作为标识
--   3. teacher 表移除登录字段       → password、account_status 迁移至 user_auth，保留 teacher_no 作为标识
--   4. student 表新增 email 字段    → 补充学生邮箱信息
--   5. 新增 admin 管理员信息表       → 支持多角色管理员（超级管理员/管理员/审计员）
--   6. course 表精简冗余字段        → 移除 week_day / class_start / class_end / week_start / week_end / schedule_type
--   7. 排课时间信息回归 course_schedule → 课程与排课分离，课程可灵活复用于多个排课安排
--   8. audit_log.user_type 扩展     → 新增 admin 类型，覆盖管理员操作审计
--
-- 【索引策略】
--   - 高频单表查询字段建立普通索引（如 status, type, name）
--   - 高频联合查询字段建立联合索引（如 user_id+status, date+status）
--   - 唯一约束天然附带唯一索引（如 student_no, course_code, username）
--   - 外键字段不需要额外建索（MySQL InnoDB 自动为外键列创建索引）
--
-- 【扩展预留】
--   - sys_config：系统级 KV 配置存储
--   - audit_log：用户操作审计日志
--   - event_outbox：本地消息表，支持 MQ 可靠投递
--   - 所有主键使用 BIGINT UNSIGNED，支持海量数据
--   - JSON 类型字段用于灵活扩展（audit_log.detail, event_outbox.payload）
-- ============================================================================