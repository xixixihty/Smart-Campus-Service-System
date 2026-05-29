-- return_book.lua
-- KEYS[1]: book:stock:{bookId}
-- KEYS[2]: book:borrowed:{userId}
-- ARGV[1]: bookId
-- ARGV[2]: userId

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 0 then
    return -1
end

redis.call('SREM', KEYS[2], ARGV[1])
redis.call('INCR', KEYS[1])
return 1