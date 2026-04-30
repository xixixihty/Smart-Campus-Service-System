-- Add capacity field to course table
USE smart_campus;

-- 1. Add capacity column to course table
ALTER TABLE course 
ADD COLUMN capacity SMALLINT UNSIGNED NULL 
COMMENT 'Course capacity (for elective/public courses only)';

-- 2. Add index for capacity field (try to create, ignore if exists)
CREATE INDEX idx_course_capacity ON course(capacity);

-- 3. Set capacity for elective courses (default 60 students)
UPDATE course 
SET capacity = 60 
WHERE type = '选修';

-- 4. Set capacity for public elective courses (default 120 students)
UPDATE course 
SET capacity = 120 
WHERE type = '公选';

-- 5. Verify the changes
SELECT id, course_code, course_name, type, capacity FROM course;
