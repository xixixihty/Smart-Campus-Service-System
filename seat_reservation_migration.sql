-- ============================================================
-- 座位预约表 migration: 添加 reservation_no 列
-- 参照 borrow_record.borrow_no 的成功模式
-- ============================================================

-- Step 1: 添加 reservation_no 列（允许 NULL，先兼容存量数据）
ALTER TABLE seat_reservation
    ADD COLUMN reservation_no VARCHAR(64) NULL COMMENT '预约编号（UUID，MQ幂等）' AFTER id;

-- Step 2: 为存量数据生成 reservation_no
UPDATE seat_reservation
SET reservation_no = CONCAT('SEAT_LEGACY_', LPAD(id, 6, '0'))
WHERE reservation_no IS NULL;

-- Step 3: 改为 NOT NULL + 添加唯一索引
ALTER TABLE seat_reservation
    MODIFY COLUMN reservation_no VARCHAR(64) NOT NULL COMMENT '预约编号（UUID，MQ幂等）',
    ADD UNIQUE KEY uk_sr_reservation_no (reservation_no);

-- Step 4: 给 status 列添加默认值（如果还没有）
ALTER TABLE seat_reservation
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT '待签到' COMMENT '状态';