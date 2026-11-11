-- ============================================================================
-- 补考模块数据迁移脚本：旧 makeup_exam → 新 makeup_exam_session + makeup_exam_student
-- 说明：本脚本为幂等设计，可重复执行。执行前请备份数据库。
-- 执行方式：登录 MySQL 后 source 本文件
-- ============================================================================

-- Step 0: 确保 makeup_exam 有 session_id 列（如未执行则取消注释）
-- ALTER TABLE makeup_exam ADD COLUMN session_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联补考场次ID' AFTER status;
-- ALTER TABLE makeup_exam ADD KEY idx_session_id (session_id);

-- Step 1: 按 (课程+学期+日期+地点) 分组，创建补考场次
INSERT INTO makeup_exam_session (course_id, semester_id, exam_date, exam_start_time, exam_end_time, location, max_capacity, enrolled_count, status)
SELECT
    se.course_id,
    se.semester_id,
    DATE(me.exam_date),
    TIME(me.exam_date),
    ADDTIME(TIME(me.exam_date), '02:00:00'),
    me.location,
    COUNT(*) AS max_capacity,
    COUNT(*) AS enrolled_count,
    CASE me.status
        WHEN '待考' THEN '已安排'
        WHEN '已考' THEN '已结束'
        WHEN '未通过' THEN '已结束'
        WHEN '取消' THEN '已取消'
        ELSE '待安排'
    END AS status
FROM makeup_exam me
JOIN score_entry se ON me.score_entry_id = se.id
WHERE me.session_id IS NULL
GROUP BY se.course_id, se.semester_id, DATE(me.exam_date), me.location;

-- Step 2: 创建学生关联记录
INSERT INTO makeup_exam_student (session_id, student_id, score_entry_id, makeup_score, student_status)
SELECT
    sess.id,
    se.student_id,
    me.score_entry_id,
    se.makeup_score,
    CASE me.status
        WHEN '待考' THEN '已通知'
        WHEN '已考' THEN '已通过'
        WHEN '未通过' THEN '未通过'
        WHEN '取消' THEN '待通知'
        ELSE '待通知'
    END AS student_status
FROM makeup_exam me
JOIN score_entry se ON me.score_entry_id = se.id
JOIN makeup_exam_session sess ON (
    sess.course_id = se.course_id
    AND sess.semester_id = se.semester_id
    AND sess.exam_date = DATE(me.exam_date)
    AND sess.location = me.location
)
WHERE me.session_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM makeup_exam_student mes
      WHERE mes.session_id = sess.id AND mes.score_entry_id = me.score_entry_id
  );

-- Step 3: 回写 makeup_exam.session_id
UPDATE makeup_exam me
JOIN makeup_exam_student mes ON me.score_entry_id = mes.score_entry_id
SET me.session_id = mes.session_id
WHERE me.session_id IS NULL;

-- Step 4: 回写 score_entry.makeup_exam_id 指向新场次
UPDATE score_entry se
JOIN makeup_exam_student mes ON se.id = mes.score_entry_id
SET se.makeup_exam_id = mes.session_id
WHERE se.makeup_exam_id IS NOT NULL
  AND se.makeup_exam_id IN (SELECT id FROM makeup_exam);

-- ============================================================================
-- 验证 SQL（迁移完成后执行，确认数据一致性）
-- ============================================================================

-- 验证1：makeup_exam_student 总数应等于 makeup_exam 记录数
-- SELECT
--     (SELECT COUNT(*) FROM makeup_exam) AS old_count,
--     (SELECT COUNT(*) FROM makeup_exam_student) AS new_count;

-- 验证2：每个场次的 enrolled_count 应与实际学生数一致
-- SELECT sess.id, sess.course_id, sess.enrolled_count,
--        (SELECT COUNT(*) FROM makeup_exam_student WHERE session_id = sess.id) AS actual_count
-- FROM makeup_exam_session sess
-- WHERE sess.enrolled_count != (SELECT COUNT(*) FROM makeup_exam_student WHERE session_id = sess.id);

-- 验证3：检查是否有未迁移的 makeup_exam 记录
-- SELECT COUNT(*) FROM makeup_exam WHERE session_id IS NULL;