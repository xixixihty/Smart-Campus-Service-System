-- borrow_book.lua
-- KEYS[1]: book:stock:{bookId}
-- KEYS[2]: book:borrowed:{userId}
-- ARGV[1]: bookId
-- ARGV[2]: userId

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -2
end

local stock = tonumber(redis.call('GET', KEYS[1]) or "0")
if stock <= 0 then
    return -1
end

redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return 1