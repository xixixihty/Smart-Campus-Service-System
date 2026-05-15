-- 座位预约模块数据库迁移脚本
USE smart_campus;

-- 1. 为seat_reservation表添加reservation_no字段（预约编号，用于MQ幂等）
ALTER TABLE seat_reservation
ADD COLUMN reservation_no VARCHAR(64) NULL AFTER id;

-- 2. 创建reservation_no唯一索引（幂等性保证）
CREATE UNIQUE INDEX idx_seat_reservation_no ON seat_reservation(reservation_no);

-- 3. 确保user_id字段存在（原脚本可能未包含）
-- 如果不存在则添加（首次建表已存在则忽略）
ALTER TABLE seat_reservation
MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '用户ID';

-- 4. 为seat_reservation表添加联合索引（优化列表查询）
CREATE INDEX idx_seat_reservation_user_date ON seat_reservation(user_id, date);
CREATE INDEX idx_seat_reservation_seat_date ON seat_reservation(seat_id, date);
CREATE INDEX idx_seat_reservation_status ON seat_reservation(status);

-- 5. 为seat表添加索引（优化布隆过滤器预热查询）
CREATE INDEX IF NOT EXISTS idx_seat_id ON seat(id);

-- 6. 验证迁移
SELECT column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'seat_reservation' AND table_schema = 'smart_campus'
ORDER BY ordinal_position;
