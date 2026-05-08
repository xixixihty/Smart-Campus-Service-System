-- borrow_book.lua
-- KEYS[1]: book:stock:{bookId}          可借库存
-- KEYS[2]: book:borrowed:{userId}       用户已借Set
-- KEYS[3]: book:waiting:{bookId}        预约排队ZSet
-- ARGV[1]: bookId
-- ARGV[2]: userId
-- ARGV[3]: timestamp

-- 1. 防重借
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -2  -- 已借未还
end

-- 2. 查库存
local stock = tonumber(redis.call('GET', KEYS[1]) or "0")
if stock <= 0 then
    -- 加入预约排队
    redis.call('ZADD', KEYS[3], ARGV[3], ARGV[2])
    return -1  -- 已借完，排队中
end

-- 3. 扣库存 + 标记已借
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return 1  -- 借阅成功
