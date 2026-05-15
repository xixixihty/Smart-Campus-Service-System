-- cancel_reservation.lua
-- KEYS[1]: seat:schedule:{seatId}:{date}  时间片ZSet
-- KEYS[2]: seat:available:{date}          空闲座位Set
-- KEYS[3]: seat:reserved:{date}           已预约Set
-- ARGV[1]: seatId
-- ARGV[2]: userId
-- ARGV[3]: startScore                    起始分钟数

-- 1. 查找并移除该用户的时间片记录
local members = redis.call('ZRANGEBYSCORE', KEYS[1], ARGV[3], ARGV[3])
local removed = false
for i, member in ipairs(members) do
    -- member格式: endScore_userId_reservationNo
    local _, uid = member:match('^(%d+)_(%d+)_')
    if uid == ARGV[2] then
        redis.call('ZREM', KEYS[1], member)
        removed = true
        break
    end
end

if not removed then
    return -1  -- 未找到预约记录
end

-- 2. 更新座位集合
redis.call('SREM', KEYS[3], ARGV[1])
redis.call('SADD', KEYS[2], ARGV[1])
return 1  -- 取消成功
