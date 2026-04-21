-- =====================================================
-- 智慧校园服务系统 数据库初始化脚本（最终完整版）
-- 包含：建库、22张表、索引、测试数据、外键约束
-- =====================================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS smart_campus;
CREATE DATABASE smart_campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_campus;

-- =====================================================
-- 一、 图书模块（6张表）
-- =====================================================

-- 1. 图书分类表
CREATE TABLE book_category (
id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
UNIQUE INDEX uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 2. 图书信息表
CREATE TABLE book (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
isbn VARCHAR(20) NOT NULL COMMENT 'ISBN编号',
title VARCHAR(200) NOT NULL COMMENT '书名',
author VARCHAR(100) NOT NULL COMMENT '作者',
publisher VARCHAR(100) COMMENT '出版社',
publish_date DATE COMMENT '出版日期',
category_id INT UNSIGNED COMMENT '分类ID',
total_copies INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '总册数',
available_copies INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '可借册数',
status VARCHAR(20) NOT NULL DEFAULT '在库' COMMENT '状态（在库/借出/维修/遗失）',
cover_image VARCHAR(500) COMMENT '封面图片URL',
description TEXT COMMENT '简介',
PRIMARY KEY (id),
INDEX idx_isbn (isbn),
INDEX idx_title (title),
INDEX idx_author (author),
INDEX idx_category_id (category_id),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书信息表';

-- 3. 借阅记录表
CREATE TABLE borrow_record (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
user_id BIGINT UNSIGNED NOT NULL COMMENT '借阅人ID',
book_id BIGINT UNSIGNED NOT NULL COMMENT '图书ID',
borrow_date DATE NOT NULL COMMENT '借书日期',
due_date DATE NOT NULL COMMENT '应还日期',
return_date DATE COMMENT '实际归还日期',
status VARCHAR(20) NOT NULL DEFAULT '借出中' COMMENT '状态（借出中/已归还/逾期）',
PRIMARY KEY (id),
INDEX idx_user_id (user_id),
INDEX idx_book_id (book_id),
INDEX idx_status (status),
INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 4. 座位信息表
CREATE TABLE seat (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
room_id BIGINT UNSIGNED NOT NULL COMMENT '阅览室ID',
seat_number VARCHAR(20) NOT NULL COMMENT '座位编号',
status VARCHAR(20) NOT NULL DEFAULT '空闲' COMMENT '状态（空闲/使用中/暂离/维修）',
PRIMARY KEY (id),
UNIQUE INDEX uk_room_seat (room_id, seat_number),
INDEX idx_room_id (room_id),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位信息表';

-- 5. 座位预约表
CREATE TABLE seat_reservation (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
user_id BIGINT UNSIGNED NOT NULL COMMENT '预约人ID',
seat_id BIGINT UNSIGNED NOT NULL COMMENT '座位ID',
date DATE NOT NULL COMMENT '预约日期',
start_time TIME NOT NULL COMMENT '开始时段',
end_time TIME NOT NULL COMMENT '结束时段',
leave_time DATETIME COMMENT '签退/暂离时间',
status VARCHAR(20) NOT NULL DEFAULT '待签到' COMMENT '状态（待签到/使用中/已完成/违约）',
PRIMARY KEY (id),
INDEX idx_user_id (user_id),
INDEX idx_seat_id (seat_id),
INDEX idx_date (date),
INDEX idx_status (status),
INDEX idx_seat_date_status (seat_id, date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位预约表';

-- 6. 阅读报告表
CREATE TABLE reading_report (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
semester VARCHAR(20) NOT NULL COMMENT '学期',
total_borrow INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总借阅量',
fav_category VARCHAR(50) COMMENT '偏好分类',
analysis_text TEXT COMMENT 'AI生成的分析文本',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
PRIMARY KEY (id),
INDEX idx_user_id (user_id),
INDEX idx_semester (semester),
INDEX idx_create_time (create_time),
UNIQUE INDEX uk_user_semester (user_id, semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读报告表';

-- =====================================================
-- 二、 教务系统模块（16张表）
-- =====================================================

-- 7. 学院表
CREATE TABLE college (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
college_code VARCHAR(20) NOT NULL COMMENT '学院代码',
college_name VARCHAR(100) NOT NULL COMMENT '学院名称',
dean VARCHAR(50) COMMENT '院长',
contact_phone VARCHAR(20) COMMENT '联系电话',
status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态（启用/禁用）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
UNIQUE INDEX uk_college_code (college_code),
INDEX idx_college_name (college_name),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

-- 8. 专业表
CREATE TABLE major (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
college_id BIGINT UNSIGNED NOT NULL COMMENT '所属学院ID',
major_code VARCHAR(20) NOT NULL COMMENT '专业代码',
major_name VARCHAR(100) NOT NULL COMMENT '专业名称',
study_years TINYINT UNSIGNED NOT NULL DEFAULT 4 COMMENT '学制年限',
status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态（启用/禁用）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
UNIQUE INDEX uk_major_code (major_code),
INDEX idx_college_id (college_id),
INDEX idx_major_name (major_name),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- 9. 班级表
CREATE TABLE class (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
major_id BIGINT UNSIGNED NOT NULL COMMENT '所属专业ID',
class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
head_teacher_id BIGINT UNSIGNED COMMENT '班主任ID',
status VARCHAR(20) NOT NULL DEFAULT '在读' COMMENT '状态（在读/毕业）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
INDEX idx_major_id (major_id),
INDEX idx_class_name (class_name),
INDEX idx_head_teacher_id (head_teacher_id),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 10. 教师表（包含登录字段）
CREATE TABLE teacher (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
teacher_no VARCHAR(20) NOT NULL COMMENT '工号（登录账号）',
name VARCHAR(50) NOT NULL COMMENT '姓名',
gender VARCHAR(10) NOT NULL COMMENT '性别（男/女）',
college_id BIGINT UNSIGNED NOT NULL COMMENT '所属学院ID',
title VARCHAR(20) COMMENT '职称',
phone VARCHAR(20) COMMENT '手机号',
email VARCHAR(100) COMMENT '邮箱',
password VARCHAR(255) NOT NULL COMMENT '登录密码（BCrypt加密）',
account_status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '账号状态（正常/锁定/禁用）',
status VARCHAR(20) NOT NULL DEFAULT '在职' COMMENT '状态（在职/离职/退休）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
UNIQUE INDEX uk_teacher_no (teacher_no),
INDEX idx_name (name),
INDEX idx_college_id (college_id),
INDEX idx_status (status),
INDEX idx_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 11. 学生表（包含登录字段）
CREATE TABLE student (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
student_no VARCHAR(20) NOT NULL COMMENT '学号（登录账号）',
name VARCHAR(50) NOT NULL COMMENT '姓名',
gender VARCHAR(10) NOT NULL COMMENT '性别（男/女）',
class_id BIGINT UNSIGNED NOT NULL COMMENT '所属班级ID',
enrollment_date DATE NOT NULL COMMENT '入学日期',
status VARCHAR(20) NOT NULL DEFAULT '在读' COMMENT '学籍状态（在读/休学/毕业/退学）',
id_card VARCHAR(18) COMMENT '身份证号',
phone VARCHAR(20) COMMENT '手机号',
password VARCHAR(255) NOT NULL COMMENT '登录密码（BCrypt加密）',
account_status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '账号状态（正常/锁定/禁用）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
UNIQUE INDEX uk_student_no (student_no),
INDEX idx_name (name),
INDEX idx_class_id (class_id),
INDEX idx_status (status),
INDEX idx_phone (phone),
INDEX idx_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 12. 教室表
CREATE TABLE classroom (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
building VARCHAR(50) NOT NULL COMMENT '教学楼',
room_number VARCHAR(20) NOT NULL COMMENT '教室门牌号',
capacity SMALLINT UNSIGNED NOT NULL COMMENT '座位容量',
type VARCHAR(20) NOT NULL DEFAULT '普通' COMMENT '类型（普通/多媒体/机房/实验室）',
status VARCHAR(20) NOT NULL DEFAULT '可用' COMMENT '状态（可用/维修/停用）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
INDEX idx_building (building),
INDEX idx_type (type),
INDEX idx_status (status),
UNIQUE INDEX uk_building_room (building, room_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室表';

-- 13. 学期表
CREATE TABLE semester (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
name VARCHAR(30) NOT NULL COMMENT '学期名称',
start_date DATE NOT NULL COMMENT '开学日期',
end_date DATE NOT NULL COMMENT '结束日期',
is_current TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否当前学期',
status VARCHAR(20) NOT NULL DEFAULT '未开始' COMMENT '状态（进行中/已结束/未开始）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
UNIQUE INDEX uk_name (name),
INDEX idx_is_current (is_current),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期表';

-- 14. 课程表
CREATE TABLE course (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
course_code VARCHAR(20) NOT NULL COMMENT '课程代码',
course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
credit DECIMAL(3,1) UNSIGNED NOT NULL COMMENT '学分',
hours TINYINT UNSIGNED NOT NULL COMMENT '总学时',
type VARCHAR(20) NOT NULL COMMENT '课程类型（必修/选修/公选）',
status VARCHAR(20) NOT NULL DEFAULT '开课' COMMENT '状态（开课/停课）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
UNIQUE INDEX uk_course_code (course_code),
INDEX idx_course_name (course_name),
INDEX idx_type (type),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 15. 排课表
CREATE TABLE course_schedule (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
semester_id BIGINT UNSIGNED NOT NULL COMMENT '学期ID',
course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
teacher_id BIGINT UNSIGNED NOT NULL COMMENT '授课教师ID',
classroom_id BIGINT UNSIGNED NOT NULL COMMENT '教室ID',
class_ids JSON NOT NULL COMMENT '授课班级ID列表(JSON数组)',
week_day TINYINT UNSIGNED NOT NULL COMMENT '星期几(1-7)',
start_section TINYINT UNSIGNED NOT NULL COMMENT '开始节次',
end_section TINYINT UNSIGNED NOT NULL COMMENT '结束节次',
week_range VARCHAR(20) NOT NULL COMMENT '周次范围(如:1-16周)',
PRIMARY KEY (id),
INDEX idx_semester_id (semester_id),
INDEX idx_course_id (course_id),
INDEX idx_teacher_id (teacher_id),
INDEX idx_classroom_id (classroom_id),
INDEX idx_week_day (week_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课表';

-- 16. 选课时间段配置表
CREATE TABLE course_selection_period (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
semester_id BIGINT UNSIGNED NOT NULL COMMENT '学期ID',
start_time DATETIME NOT NULL COMMENT '选课开始时间',
end_time DATETIME NOT NULL COMMENT '选课结束时间',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
INDEX idx_semester_id (semester_id),
INDEX idx_start_end_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课时间段配置表';

-- 17. 选课表
CREATE TABLE course_selection (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
student_id BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
semester_id BIGINT UNSIGNED NOT NULL COMMENT '学期ID',
status VARCHAR(20) NOT NULL DEFAULT '已选' COMMENT '状态（已选/退课）',
score DECIMAL(5,2) COMMENT '最终成绩',
score_point DECIMAL(3,2) COMMENT '绩点',
PRIMARY KEY (id),
INDEX idx_student_id (student_id),
INDEX idx_course_id (course_id),
INDEX idx_semester_id (semester_id),
INDEX idx_status (status),
UNIQUE INDEX uk_student_course_semester (student_id, course_id, semester_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课表';

-- 18. 成绩录入表
CREATE TABLE score_entry (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
student_id BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
semester_id BIGINT UNSIGNED NOT NULL COMMENT '学期ID',
usual_score DECIMAL(5,2) COMMENT '平时成绩',
final_score DECIMAL(5,2) COMMENT '期末成绩',
total_score DECIMAL(5,2) COMMENT '总评成绩',
makeup_score DECIMAL(5,2) COMMENT '补考成绩',
exam_status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '考试状态（正常/缓考/缺考/作弊/补考）',
PRIMARY KEY (id),
INDEX idx_course_id (course_id),
INDEX idx_student_id (student_id),
INDEX idx_semester_id (semester_id),
INDEX idx_exam_status (exam_status),
UNIQUE INDEX uk_student_course_semester (student_id, course_id, semester_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩录入表';

-- 19. 请假申请表
CREATE TABLE leave_request (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
student_id BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
leave_type VARCHAR(20) NOT NULL COMMENT '请假类型（事假/病假/公假）',
start_time DATETIME NOT NULL COMMENT '开始时间',
end_time DATETIME NOT NULL COMMENT '结束时间',
reason VARCHAR(500) NOT NULL COMMENT '请假事由',
status VARCHAR(20) NOT NULL DEFAULT '待审批' COMMENT '状态（待审批/已批准/已驳回）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
PRIMARY KEY (id),
INDEX idx_student_id (student_id),
INDEX idx_status (status),
INDEX idx_create_time (create_time),
INDEX idx_student_status (student_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 20. 请假审批记录表
CREATE TABLE leave_approval_log (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
leave_request_id BIGINT UNSIGNED NOT NULL COMMENT '请假申请ID',
approver_id BIGINT UNSIGNED NOT NULL COMMENT '审批人ID',
approve_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
result VARCHAR(20) NOT NULL COMMENT '审批结果（批准/驳回）',
PRIMARY KEY (id),
INDEX idx_leave_request_id (leave_request_id),
INDEX idx_approver_id (approver_id),
INDEX idx_approve_time (approve_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假审批记录表';

-- 21. 通知公告表
CREATE TABLE notice (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
title VARCHAR(200) NOT NULL COMMENT '标题',
content TEXT NOT NULL COMMENT '正文内容',
publisher_id BIGINT UNSIGNED NOT NULL COMMENT '发布人ID',
publish_time DATETIME COMMENT '发布时间',
target_type VARCHAR(20) NOT NULL DEFAULT '全部' COMMENT '发布范围类型（全部/学院/专业/班级）',
status VARCHAR(20) NOT NULL DEFAULT '发布' COMMENT '状态（发布/撤回）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
INDEX idx_title (title),
INDEX idx_publisher_id (publisher_id),
INDEX idx_publish_time (publish_time),
INDEX idx_target_type (target_type),
INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- 22. 补考安排表
CREATE TABLE makeup_exam (
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
score_entry_id BIGINT UNSIGNED NOT NULL COMMENT '关联的成绩记录ID',
exam_date DATE NOT NULL COMMENT '补考日期',
start_time TIME NOT NULL COMMENT '开始时间',
end_time TIME NOT NULL COMMENT '结束时间',
status VARCHAR(20) NOT NULL DEFAULT '待考' COMMENT '状态（待考/已考/取消）',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (id),
INDEX idx_score_entry_id (score_entry_id),
INDEX idx_status (status),
UNIQUE INDEX uk_score_entry (score_entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补考安排表';

-- =====================================================
-- 三、 测试数据插入
-- =====================================================

-- 图书分类表数据
INSERT INTO book_category (id, category_name) VALUES
(1, '计算机科学'),
(2, '文学小说'),
(3, '历史地理'),
(4, '经济管理'),
(5, '编程语言'),
(6, '数据库'),
(7, '人工智能'),
(8, '中国文学'),
(9, '外国文学');

-- 图书信息表数据
INSERT INTO book (id, isbn, title, author, publisher, publish_date, category_id, total_copies, available_copies, status, cover_image, description) VALUES
(1, '9787111638436', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '2019-01-01', 5, 5, 3, '在库', '/covers/java.jpg', 'Java经典著作'),
(2, '9787115472778', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', '2020-05-01', 6, 3, 2, '在库', '/covers/mysql.jpg', 'MySQL入门经典'),
(3, '9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', '2008-07-01', 8, 10, 8, '在库', '/covers/hlm.jpg', '中国古典四大名著之一'),
(4, '9787506365437', '活着', '余华', '作家出版社', '2012-08-01', 8, 5, 4, '在库', '/covers/huozhe.jpg', '余华代表作'),
(5, '9787302523286', '深度学习', 'Ian Goodfellow', '人民邮电出版社', '2019-09-01', 7, 2, 1, '在库', '/covers/dl.jpg', 'AI领域经典教材'),
(6, '9787121348469', 'Spring Boot实战', 'Craig Walls', '人民邮电出版社', '2018-06-01', 5, 4, 4, '在库', '/covers/springboot.jpg', 'Spring Boot入门与实践');

-- 学院表数据
INSERT INTO college (id, college_code, college_name, dean, contact_phone, status) VALUES
(1, 'CS', '计算机科学与技术学院', '张明', '13800138001', '启用'),
(2, 'MATH', '数学与统计学院', '李华', '13800138002', '启用'),
(3, 'ENGL', '外国语学院', '王芳', '13800138003', '启用');

-- 专业表数据
INSERT INTO major (id, college_id, major_code, major_name, study_years, status) VALUES
(1, 1, 'CS001', '计算机科学与技术', 4, '启用'),
(2, 1, 'SE001', '软件工程', 4, '启用'),
(3, 2, 'MATH001', '数学与应用数学', 4, '启用'),
(4, 3, 'ENG001', '英语语言文学', 4, '启用');

-- 教师表数据（密码密文对应明文：123456）
INSERT INTO teacher (id, teacher_no, name, gender, college_id, title, phone, email, password, account_status, status) VALUES
(1, 'T2021001', '张伟', '男', 1, '教授', '13900139001', 'zhangwei@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常', '在职'),
(2, 'T2021002', '刘静', '女', 1, '副教授', '13900139002', 'liujing@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常', '在职'),
(3, 'T2021003', '王强', '男', 2, '讲师', '13900139003', 'wangqiang@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常', '在职'),
(4, 'T2021004', '陈丽', '女', 3, '副教授', '13900139004', 'chenli@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常', '在职'),
(5, 'T2021005', '赵磊', '男', 1, '讲师', '13900139005', 'zhaolei@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常', '在职');

-- 班级表数据
INSERT INTO class (id, major_id, class_name, head_teacher_id, status) VALUES
(1, 1, '计算机2023级1班', 1, '在读'),
(2, 2, '软件工程2023级1班', 2, '在读'),
(3, 1, '计算机2022级1班', 5, '在读'),
(4, 3, '数学2023级1班', 3, '在读'),
(5, 4, '英语2023级1班', 4, '在读');

-- 学生表数据（密码密文对应明文：123456）
INSERT INTO student (id, student_no, name, gender, class_id, enrollment_date, status, id_card, phone, password, account_status) VALUES
(1, '2023001001', '李明', '男', 1, '2023-09-01', '在读', '110101200501011234', '15000150001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常'),
(2, '2023001002', '王红', '女', 1, '2023-09-01', '在读', '110101200502022345', '15000150002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常'),
(3, '2023002001', '赵阳', '男', 2, '2023-09-01', '在读', '110101200503033456', '15000150003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常'),
(4, '2022001001', '孙丽', '女', 3, '2022-09-01', '在读', '110101200401014567', '15000150004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常'),
(5, '2023003001', '周杰', '男', 4, '2023-09-01', '在读', '110101200502045678', '15000150005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常'),
(6, '2023004001', '吴迪', '女', 5, '2023-09-01', '在读', '110101200503056789', '15000150006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '正常');

-- 教室表数据
INSERT INTO classroom (id, building, room_number, capacity, type, status) VALUES
(1, '博学楼', 'A101', 80, '多媒体', '可用'),
(2, '博学楼', 'A102', 60, '多媒体', '可用'),
(3, '博学楼', 'A201', 40, '普通', '可用'),
(4, '明理楼', 'B301', 100, '多媒体', '可用'),
(5, '明理楼', 'B302', 120, '机房', '可用');

-- 学期表数据
INSERT INTO semester (id, name, start_date, end_date, is_current, status) VALUES
(1, '2023-2024第一学期', '2023-09-01', '2024-01-15', 0, '已结束'),
(2, '2023-2024第二学期', '2024-02-25', '2024-07-10', 1, '进行中');

-- 课程表数据
INSERT INTO course (id, course_code, course_name, credit, hours, type, status) VALUES
(1, 'CS101', '计算机导论', 3.0, 48, '必修', '开课'),
(2, 'CS102', 'Java程序设计', 4.0, 64, '必修', '开课'),
(3, 'CS201', '数据结构', 4.0, 64, '必修', '开课'),
(4, 'SE101', '软件工程概论', 3.0, 48, '必修', '开课'),
(5, 'MATH101', '高等数学', 5.0, 80, '必修', '开课'),
(6, 'CS301', '人工智能导论', 2.0, 32, '选修', '开课'),
(7, 'ENG101', '大学英语', 4.0, 64, '公选', '开课');

-- 排课表数据
INSERT INTO course_schedule (id, semester_id, course_id, teacher_id, classroom_id, class_ids, week_day, start_section, end_section, week_range) VALUES
(1, 2, 2, 1, 1, '[1]', 1, 1, 2, '1-16周'),
(2, 2, 3, 2, 2, '[1]', 3, 3, 4, '1-16周'),
(3, 2, 4, 5, 3, '[2]', 2, 1, 2, '1-16周'),
(4, 2, 5, 3, 4, '[4]', 4, 1, 2, '1-16周'),
(5, 2, 7, 4, 1, '[1,2,4]', 5, 5, 6, '1-16周'),
(6, 2, 6, 2, 5, '[1,2]', 1, 7, 8, '1-8周');

-- 选课时间段配置表数据
INSERT INTO course_selection_period (id, semester_id, start_time, end_time) VALUES
(1, 2, '2024-02-20 09:00:00', '2024-02-25 23:59:59');

-- 选课表数据
INSERT INTO course_selection (id, student_id, course_id, semester_id, status, score, score_point) VALUES
(1, 1, 6, 2, '已选', 85.5, 3.5),
(2, 2, 6, 2, '已选', 90.0, 4.0),
(3, 3, 6, 2, '已选', NULL, NULL),
(4, 1, 7, 2, '已选', 78.0, 3.0),
(5, 4, 7, 2, '已选', 82.0, 3.3);

-- 成绩录入表数据
INSERT INTO score_entry (id, course_id, student_id, semester_id, usual_score, final_score, total_score, makeup_score, exam_status) VALUES
(1, 2, 1, 2, 90, 85, 87.5, NULL, '正常'),
(2, 2, 2, 2, 88, 92, 90.0, NULL, '正常'),
(3, 3, 1, 2, 75, 80, 77.5, NULL, '正常'),
(4, 5, 5, 2, 60, 55, 57.0, NULL, '缺考'),
(5, 6, 1, 2, 95, 90, 92.5, NULL, '正常'),
(6, 6, 2, 2, 90, 92, 91.0, NULL, '正常');

-- 请假申请表数据
INSERT INTO leave_request (id, student_id, leave_type, start_time, end_time, reason, status, create_time) VALUES
(1, 1, '事假', '2024-06-10 08:00:00', '2024-06-12 17:00:00', '回家处理家庭事务', '已批准', '2024-06-05 10:30:00'),
(2, 2, '病假', '2024-06-11 08:00:00', '2024-06-11 17:00:00', '感冒发烧就医', '已批准', '2024-06-10 23:00:00'),
(3, 3, '事假', '2024-06-15 08:00:00', '2024-06-16 17:00:00', '参加竞赛', '待审批', '2024-06-12 14:20:00');

-- 请假审批记录表数据
INSERT INTO leave_approval_log (id, leave_request_id, approver_id, approve_time, result) VALUES
(1, 1, 1, '2024-06-05 14:00:00', '批准'),
(2, 2, 1, '2024-06-11 08:30:00', '批准');

-- 通知公告表数据
INSERT INTO notice (id, title, content, publisher_id, publish_time, target_type, status) VALUES
(1, '关于期末考试安排的通知', '本学期期末考试将于第18周进行，请各位同学认真复习。', 1, '2024-06-01 09:00:00', '全部', '发布'),
(2, '图书馆临时闭馆通知', '因电力检修，图书馆6月15日闭馆一天。', 1, '2024-06-10 10:00:00', '全部', '发布'),
(3, '计算机学院学术讲座通知', '邀请李教授做AI前沿报告，时间：6月20日14:00，地点：博学楼A101', 2, '2024-06-11 08:00:00', '学院', '发布');

-- 座位信息表数据
INSERT INTO seat (id, room_id, seat_number, status) VALUES
(1, 1, 'A001', '空闲'),
(2, 1, 'A002', '空闲'),
(3, 1, 'A003', '使用中'),
(4, 2, 'B001', '空闲'),
(5, 2, 'B002', '暂离'),
(6, 2, 'B003', '维修');

-- 座位预约表数据
INSERT INTO seat_reservation (id, user_id, seat_id, date, start_time, end_time, leave_time, status) VALUES
(1, 1, 3, '2024-06-15', '09:00:00', '12:00:00', NULL, '使用中'),
(2, 2, 5, '2024-06-15', '14:00:00', '17:00:00', '2024-06-15 15:30:00', '暂离'),
(3, 3, 1, '2024-06-16', '10:00:00', '12:00:00', NULL, '待签到');

-- 借阅记录表数据
INSERT INTO borrow_record (id, user_id, book_id, borrow_date, due_date, return_date, status) VALUES
(1, 1, 1, '2024-06-01', '2024-06-30', NULL, '借出中'),
(2, 2, 2, '2024-05-15', '2024-06-14', '2024-06-10', '已归还'),
(3, 3, 3, '2024-05-20', '2024-06-19', NULL, '借出中'),
(4, 1, 4, '2024-04-01', '2024-04-30', '2024-05-05', '逾期'),
(5, 4, 5, '2024-06-05', '2024-07-04', NULL, '借出中');

-- 阅读报告表数据
INSERT INTO reading_report (id, user_id, semester, total_borrow, fav_category, analysis_text, create_time) VALUES
(1, 1, '2023-2024第二学期', 8, '编程语言', '该生借阅偏好集中于计算机编程类书籍，具有明确的学习方向。', '2024-06-01 10:00:00'),
(2, 2, '2023-2024第二学期', 5, '中国文学', '该生喜爱阅读经典文学作品，人文素养较好。', '2024-06-01 10:05:00');

-- 补考安排表数据
INSERT INTO makeup_exam (id, score_entry_id, exam_date, start_time, end_time, status) VALUES
(1, 4, '2024-07-05', '14:00:00', '16:00:00', '待考');

-- =====================================================
-- 四、 外键约束
-- =====================================================

-- 图书模块外键
ALTER TABLE book ADD CONSTRAINT fk_book_category_id FOREIGN KEY (category_id) REFERENCES book_category(id);
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_record_user_id FOREIGN KEY (user_id) REFERENCES student(id);
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_record_book_id FOREIGN KEY (book_id) REFERENCES book(id);
ALTER TABLE seat_reservation ADD CONSTRAINT fk_seat_reservation_user_id FOREIGN KEY (user_id) REFERENCES student(id);
ALTER TABLE seat_reservation ADD CONSTRAINT fk_seat_reservation_seat_id FOREIGN KEY (seat_id) REFERENCES seat(id);
ALTER TABLE reading_report ADD CONSTRAINT fk_reading_report_user_id FOREIGN KEY (user_id) REFERENCES student(id);

-- 教务模块外键
ALTER TABLE major ADD CONSTRAINT fk_major_college_id FOREIGN KEY (college_id) REFERENCES college(id);
ALTER TABLE class ADD CONSTRAINT fk_class_major_id FOREIGN KEY (major_id) REFERENCES major(id);
ALTER TABLE class ADD CONSTRAINT fk_class_head_teacher_id FOREIGN KEY (head_teacher_id) REFERENCES teacher(id);
ALTER TABLE teacher ADD CONSTRAINT fk_teacher_college_id FOREIGN KEY (college_id) REFERENCES college(id);
ALTER TABLE student ADD CONSTRAINT fk_student_class_id FOREIGN KEY (class_id) REFERENCES class(id);
ALTER TABLE course_schedule ADD CONSTRAINT fk_course_schedule_semester_id FOREIGN KEY (semester_id) REFERENCES semester(id);
ALTER TABLE course_schedule ADD CONSTRAINT fk_course_schedule_course_id FOREIGN KEY (course_id) REFERENCES course(id);
ALTER TABLE course_schedule ADD CONSTRAINT fk_course_schedule_teacher_id FOREIGN KEY (teacher_id) REFERENCES teacher(id);
ALTER TABLE course_schedule ADD CONSTRAINT fk_course_schedule_classroom_id FOREIGN KEY (classroom_id) REFERENCES classroom(id);
ALTER TABLE course_selection_period ADD CONSTRAINT fk_course_selection_period_semester_id FOREIGN KEY (semester_id) REFERENCES semester(id);
ALTER TABLE course_selection ADD CONSTRAINT fk_course_selection_student_id FOREIGN KEY (student_id) REFERENCES student(id);
ALTER TABLE course_selection ADD CONSTRAINT fk_course_selection_course_id FOREIGN KEY (course_id) REFERENCES course(id);
ALTER TABLE course_selection ADD CONSTRAINT fk_course_selection_semester_id FOREIGN KEY (semester_id) REFERENCES semester(id);
ALTER TABLE score_entry ADD CONSTRAINT fk_score_entry_course_id FOREIGN KEY (course_id) REFERENCES course(id);
ALTER TABLE score_entry ADD CONSTRAINT fk_score_entry_student_id FOREIGN KEY (student_id) REFERENCES student(id);
ALTER TABLE score_entry ADD CONSTRAINT fk_score_entry_semester_id FOREIGN KEY (semester_id) REFERENCES semester(id);
ALTER TABLE leave_request ADD CONSTRAINT fk_leave_request_student_id FOREIGN KEY (student_id) REFERENCES student(id);
ALTER TABLE leave_approval_log ADD CONSTRAINT fk_leave_approval_log_request_id FOREIGN KEY (leave_request_id) REFERENCES leave_request(id);
ALTER TABLE leave_approval_log ADD CONSTRAINT fk_leave_approval_log_approver_id FOREIGN KEY (approver_id) REFERENCES teacher(id);
ALTER TABLE notice ADD CONSTRAINT fk_notice_publisher_id FOREIGN KEY (publisher_id) REFERENCES teacher(id);
ALTER TABLE makeup_exam ADD CONSTRAINT fk_makeup_exam_score_entry_id FOREIGN KEY (score_entry_id) REFERENCES score_entry(id);