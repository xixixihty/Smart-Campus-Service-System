-- 排课管理模块数据库索引优化脚本

-- 1. 为course_schedule表添加索引
-- 主键索引（如果不存在）
ALTER TABLE course_schedule ADD PRIMARY KEY IF NOT EXISTS (id);

-- 学期索引（用于按学期查询）
CREATE INDEX idx_course_schedule_semester ON course_schedule(semester_id);

-- 课程索引（用于按课程查询）
CREATE INDEX idx_course_schedule_course ON course_schedule(course_id);

-- 教师索引（用于按教师查询和冲突检测）
CREATE INDEX idx_course_schedule_teacher ON course_schedule(teacher_id);

-- 教室索引（用于按教室查询和冲突检测）
CREATE INDEX idx_course_schedule_classroom ON course_schedule(classroom_id);

-- 星期和节次索引（用于时间冲突检测）
CREATE INDEX idx_course_schedule_time ON course_schedule(week_day, start_section, end_section);

-- 复合索引（用于排课列表查询优化）
CREATE INDEX idx_course_schedule_semester_teacher ON course_schedule(semester_id, teacher_id);
CREATE INDEX idx_course_schedule_semester_classroom ON course_schedule(semester_id, classroom_id);
CREATE INDEX idx_course_schedule_semester_week ON course_schedule(semester_id, week_day);

-- 2. 为相关表添加索引（如果不存在）
-- 课程表
CREATE INDEX IF NOT EXISTS idx_course_name ON course(course_name);

-- 教师表
CREATE INDEX IF NOT EXISTS idx_teacher_name ON teacher(teacher_name);

-- 教室表
CREATE INDEX IF NOT EXISTS idx_classroom_name ON classroom(classroom_name);

-- 班级表
CREATE INDEX IF NOT EXISTS idx_class_name ON class(class_name);

-- 学期表
CREATE INDEX IF NOT EXISTS idx_semester_name ON semester(semester_name);

-- 3. 为student表添加索引（用于学生课表查询）
CREATE INDEX IF NOT EXISTS idx_student_class ON student(class_id);

-- 4. 优化建议
-- 定期分析表统计信息
ANALYZE TABLE course_schedule;
ANALYZE TABLE course;
ANALYZE TABLE teacher;
ANALYZE TABLE classroom;
ANALYZE TABLE class;
ANALYZE TABLE semester;
ANALYZE TABLE student;