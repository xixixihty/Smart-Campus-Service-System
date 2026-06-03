use smart_campus

-- ============================================================================
-- admin 管理员表（新建）
-- 设计说明：
--   1. college_id 将管理员与学院关联，实现「学院管理员审批本学院教师请假」
--   2. super_admin role 无 college_id，可审批所有请假（兜底机制）
--   3. 使用 BCrypt 哈希存储密码，与现有 student/teacher 密码机制一致
--   4. account_status 支持「启用/锁定/禁用」三种状态
-- ============================================================================
CREATE TABLE IF NOT EXISTS admin (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '管理员ID',
    admin_no        VARCHAR(32)       NOT NULL                 COMMENT '管理员账号（登录用）',
    name            VARCHAR(50)       NOT NULL                 COMMENT '姓名',
    gender          VARCHAR(10)       NOT NULL DEFAULT '男'    COMMENT '性别',
    role            VARCHAR(20)       NOT NULL DEFAULT '管理员' 
                    COMMENT '角色：超级管理员 / 管理员 / 审计员',
    college_id      BIGINT UNSIGNED   DEFAULT NULL             
                    COMMENT '所属学院ID（管理员关联学院，超级管理员为NULL）',
    phone           VARCHAR(20)       DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(100)      DEFAULT NULL             COMMENT '邮箱',
    account_status  VARCHAR(20)       NOT NULL DEFAULT '启用'  
                    COMMENT '账号状态：启用 / 锁定 / 禁用',
    password        VARCHAR(255)      NOT NULL                 COMMENT 'BCrypt哈希密码',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP 
                    ON UPDATE CURRENT_TIMESTAMP                COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_no (admin_no),
    KEY idx_admin_college (college_id),
    KEY idx_admin_role (role),
    KEY idx_admin_status (account_status),
    KEY idx_admin_email (email),
    CONSTRAINT fk_admin_college FOREIGN KEY (college_id) REFERENCES college (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 初始化一个超级管理员
INSERT INTO admin (admin_no, name, role, college_id, account_status, password)
VALUES ('admin', '系统管理员', '超级管理员', NULL, '启用', 
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs');
        
        -- ============================================================================
-- leave_request 请假申请表 — 改造为统一申请表
-- 设计说明：
--   1. applicant_type + applicant_id 取代原先单一的 student_id
--   2. approver_id 在提交时由系统自动匹配填充
--   3. is_course_rescheduled 标记教师请假是否需要调课
--   4. student_id 保留但改为可空，兼容旧数据和学生端快捷查询
-- ============================================================================

-- Step 1：新增字段
ALTER TABLE leave_request
    ADD COLUMN applicant_type          VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'
        COMMENT '申请人类型：STUDENT / TEACHER' AFTER id,
    ADD COLUMN applicant_id            BIGINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT '申请人ID（学生ID或教师ID）' AFTER applicant_type,
    ADD COLUMN approver_id             BIGINT UNSIGNED DEFAULT NULL
        COMMENT '审批人ID（教师ID 或 管理员ID）' AFTER applicant_id,
    ADD COLUMN is_course_rescheduled   TINYINT(1)   NOT NULL DEFAULT 0
        COMMENT '是否需要调课：0-否，1-是' AFTER status;

-- Step 2：兼容旧数据（旧 student_id 回填到 applicant_id）
UPDATE leave_request 
SET applicant_id = student_id 
WHERE applicant_id = 0 AND student_id IS NOT NULL;

-- Step 3：student_id 改为可空
ALTER TABLE leave_request
    MODIFY COLUMN student_id BIGINT UNSIGNED NULL
        COMMENT '学生ID（仅学生请假时填充，教师请假为NULL）';

-- Step 4：新增索引
ALTER TABLE leave_request
    ADD INDEX idx_lr_applicant (applicant_type, applicant_id),
    ADD INDEX idx_lr_approver (approver_id),
    ADD INDEX idx_lr_approver_status (approver_id, status);

-- Step 5：student_id 外键约束调整（因改为可空，外键在原约束中已存在无需改动）

-- ============================================================================
-- course_reschedule 调课记录表（新建）
-- 设计说明：
--   1. 一条请假可对应多条调课记录（一个老师可能有多门课需要调）
--   2. 同时保存原始安排和目标安排，便于审计和回滚
--   3. 调课方向约束为「往后调」，即 new_week_day >= original_week_day
--   4. 冲突检测在应用层完成，数据库不做唯一约束（同一时段可排多门课）
-- ============================================================================
CREATE TABLE IF NOT EXISTS course_reschedule (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    leave_request_id        BIGINT UNSIGNED NOT NULL                 COMMENT '请假申请ID',
    course_schedule_id      BIGINT UNSIGNED NOT NULL                 COMMENT '原排课记录ID',
    teacher_id              BIGINT UNSIGNED NOT NULL                 COMMENT '调课教师ID',
    
    -- 原课程快照
    original_week_day       TINYINT UNSIGNED NOT NULL                COMMENT '原星期（1-7）',
    original_start_section  TINYINT UNSIGNED NOT NULL                COMMENT '原起始节次',
    original_end_section    TINYINT UNSIGNED NOT NULL                COMMENT '原结束节次',
    original_classroom_id   BIGINT UNSIGNED DEFAULT NULL             COMMENT '原教室ID',
    
    -- 新课程安排
    new_week_day            TINYINT UNSIGNED NOT NULL                COMMENT '新星期',
    new_start_section       TINYINT UNSIGNED NOT NULL                COMMENT '新起始节次',
    new_end_section         TINYINT UNSIGNED NOT NULL                COMMENT '新结束节次',
    new_classroom_id        BIGINT UNSIGNED DEFAULT NULL             COMMENT '新教室ID',
    
    status                  VARCHAR(20) NOT NULL DEFAULT '待确认'    
                            COMMENT '状态：待确认 / 已确认 / 已取消',
    remark                  VARCHAR(500) DEFAULT NULL                COMMENT '备注',
    
    create_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    KEY idx_cr_leave (leave_request_id),
    KEY idx_cr_course (course_schedule_id),
    KEY idx_cr_teacher (teacher_id),
    KEY idx_cr_status (status),
    CONSTRAINT fk_cr_leave FOREIGN KEY (leave_request_id) 
        REFERENCES leave_request(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cr_schedule FOREIGN KEY (course_schedule_id) 
        REFERENCES course_schedule(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cr_teacher FOREIGN KEY (teacher_id) 
        REFERENCES teacher(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
  COMMENT='调课记录表';
  
  -- ============================================================================
-- notification 实时通知表（新建）
-- 设计说明：
--   1. 存储所有请假、审批、调课相关的实时通知
--   2. WebSocket 断连时不丢消息，重连后从表中拉取未读
--   3. 30天自动清理旧通知（定时任务）
--   4. 与现有的 notice (系统公告) 表职责不同，此为个性化推送通知
-- ============================================================================
CREATE TABLE IF NOT EXISTS notification (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT     COMMENT '主键',
    recipient_id    BIGINT UNSIGNED NOT NULL                    COMMENT '接收人ID',
    recipient_type  VARCHAR(20)     NOT NULL                    
                    COMMENT '接收人类型：STUDENT / TEACHER / ADMIN',
    
    title           VARCHAR(200)    NOT NULL                    COMMENT '通知标题',
    content         TEXT            NOT NULL                    COMMENT '通知内容',
    
    biz_type        VARCHAR(50)     NOT NULL                    
                    COMMENT '业务类型：
                            LEAVE_APPLY        — 新请假申请
                            LEAVE_APPROVED     — 请假已批准
                            LEAVE_REJECTED     — 请假已驳回
                            NEED_RESCHEDULE    — 需要调课
                            COURSE_RESCHEDULED — 课程已调整',
    biz_id          BIGINT UNSIGNED DEFAULT NULL                COMMENT '关联业务ID',
    
    is_read         TINYINT(1)      NOT NULL DEFAULT 0          COMMENT '0-未读，1-已读',
    read_time       DATETIME        DEFAULT NULL                COMMENT '阅读时间',
    
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    KEY idx_n_recipient (recipient_id, recipient_type),
    KEY idx_n_unread (recipient_id, recipient_type, is_read),
    KEY idx_n_biz (biz_type, biz_id),
    KEY idx_n_time (create_time)            -- 用于30天定时清理
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
  COMMENT='实时通知表（30天自动清理）';

-- 定时清理SQL（由定时任务执行）
-- DELETE FROM notification WHERE create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- ============================================================================
-- leave_approval_log 审批日志表 — 外键调整
-- 当前 approver_id 外键仅关联 teacher，需要同时支持 admin
-- 策略：移除外键约束，改为应用层保证数据一致性
-- （因为 approver_id 可能来自 teacher 表或 admin 表，无法建单一外键）
-- ============================================================================

-- 移除旧的 teacher 外键
ALTER TABLE leave_approval_log 
    DROP FOREIGN KEY fk_lal_approver;

-- 保留索引（已有 idx_lal_approver）