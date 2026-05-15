-- reserve_seat.lua
-- KEYS[1]: seat:schedule:{seatId}:{date}  时间片ZSet
-- KEYS[2]: seat:available:{date}          空闲座位Set
-- KEYS[3]: seat:reserved:{date}           已预约Set
-- ARGV[1]: startScore                    起始分钟数
-- ARGV[2]: endScore                      结束分钟数
-- ARGV[3]: seatId
-- ARGV[4]: userId
-- ARGV[5]: reservationNo                 预约流水号

-- 1. 时间冲突检测：检查该时段是否已被预约
local existing = redis.call('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'LIMIT', 0, 1)
if #existing > 0 then
    return 0  -- 冲突
end

-- 2. 无冲突，写入时间片
-- member格式: 结束分钟数_用户ID_预约流水号
local member = ARGV[2] .. '_' .. ARGV[4] .. '_' .. ARGV[5]
redis.call('ZADD', KEYS[1], ARGV[1], member)

-- 3. 更新座位集合
redis.call('SREM', KEYS[2], ARGV[3])
redis.call('SADD', KEYS[3], ARGV[3])
return 1  -- 预约成功
