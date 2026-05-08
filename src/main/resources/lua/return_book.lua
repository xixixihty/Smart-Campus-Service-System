-- return_book.lua
-- KEYS[1]: book:stock:{bookId}          可借库存
-- KEYS[2]: book:borrowed:{userId}       用户已借Set
-- KEYS[3]: book:waiting:{bookId}        预约排队ZSet
-- ARGV[1]: bookId
-- ARGV[2]: userId

-- 1. 检查是否已借
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 0 then
    return -1   -- 未借过该书
end

-- 2. 移除已借标记 + 释放库存
redis.call('SREM', KEYS[2], ARGV[1])
redis.call('INCR', KEYS[1])

-- 3. 取预约排队队首
local waiters = redis.call('ZPOPMIN', KEYS[3], 1)
if waiters and #waiters > 0 then
    local nextUser = waiters[1]
    -- 自动补位：扣库存 + 标记已借
    redis.call('DECR', KEYS[1])
    redis.call('SADD', KEYS[2], ARGV[1])
    return nextUser   -- 返回候补成功者userId
end
return 1   -- 归还成功，无排队
