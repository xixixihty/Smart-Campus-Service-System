-- ============================================================================
-- 数据迁移脚本：course_schedule.class_ids JSON → course_schedule_class 关联表
-- 适用数据库：MySQL 8.0+
-- 执行前请务必备份数据库！
-- ============================================================================

-- ============================================================================
-- 第一步：创建 course_schedule_class 关联表（如果尚未创建）
-- ============================================================================
CREATE TABLE IF NOT EXISTS course_schedule_class (
    id          BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    schedule_id BIGINT UNSIGNED   NOT NULL                 COMMENT '排课ID',
    class_id    BIGINT UNSIGNED   NOT NULL                 COMMENT '班级ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_class (schedule_id, class_id),
    KEY idx_schedule_id (schedule_id),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课-班级关联表';

-- ============================================================================
-- 第二步：将 class_ids JSON 数组拆分为关联表记录
-- 使用 JSON_TABLE 函数（MySQL 8.0+）
-- ============================================================================
INSERT INTO course_schedule_class (schedule_id, class_id)
SELECT
    cs.id AS schedule_id,
    jt.class_id AS class_id
FROM course_schedule cs
CROSS JOIN JSON_TABLE(
    cs.class_ids,
    '$[*]' COLUMNS(class_id BIGINT UNSIGNED PATH '$')
) AS jt
WHERE cs.class_ids IS NOT NULL
  AND JSON_LENGTH(cs.class_ids) > 0
ON DUPLICATE KEY UPDATE class_id = VALUES(class_id);

-- ============================================================================
-- 第三步：验证迁移结果
-- ============================================================================
-- 检查迁移前后的班级数量是否一致
SELECT
    'JSON原始班级数' AS 来源,
    SUM(JSON_LENGTH(class_ids)) AS 班级总数
FROM course_schedule
WHERE class_ids IS NOT NULL
UNION ALL
SELECT
    '关联表班级数' AS 来源,
    COUNT(*) AS 班级总数
FROM course_schedule_class;

-- ============================================================================
-- 第四步：创建 score_audit_log 审计日志表
-- ============================================================================
CREATE TABLE IF NOT EXISTS score_audit_log (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键',
    score_entry_id  BIGINT UNSIGNED NOT NULL                 COMMENT '成绩记录ID',
    course_id       BIGINT UNSIGNED NOT NULL                 COMMENT '课程ID',
    student_id      BIGINT UNSIGNED NOT NULL                 COMMENT '学生ID',
    operator_id     BIGINT UNSIGNED NOT NULL                 COMMENT '操作人ID',
    operator_type   VARCHAR(20)     NOT NULL                 COMMENT '操作人类型(admin/teacher)',
    old_usual_score DECIMAL(5,2)    DEFAULT NULL             COMMENT '原平时成绩',
    new_usual_score DECIMAL(5,2)    DEFAULT NULL             COMMENT '新平时成绩',
    old_final_score DECIMAL(5,2)    DEFAULT NULL             COMMENT '原期末成绩',
    new_final_score DECIMAL(5,2)    DEFAULT NULL             COMMENT '新期末成绩',
    old_total_score DECIMAL(5,2)    DEFAULT NULL             COMMENT '原总评成绩',
    new_total_score DECIMAL(5,2)    DEFAULT NULL             COMMENT '新总评成绩',
    operation       VARCHAR(20)     NOT NULL                 COMMENT '操作类型(INSERT/UPDATE)',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_score_entry_id (score_entry_id),
    KEY idx_course_id (course_id),
    KEY idx_operator_id (operator_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩审计日志表';

-- ============================================================================
-- 第五步（可选，确认无误后执行）：删除 course_schedule.class_ids 列
-- 注意：此操作不可逆，请确保上述迁移验证通过后再执行！
-- ============================================================================
-- ALTER TABLE course_schedule DROP COLUMN class_ids;