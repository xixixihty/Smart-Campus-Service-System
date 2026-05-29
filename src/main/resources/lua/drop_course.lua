local stock_key = KEYS[1]
local selected_key = KEYS[2]
local waiting_key = KEYS[3]
local waiting_count_key = KEYS[4]
local course_id = ARGV[1]
local user_id = ARGV[2]

local selected = redis.call('SISMEMBER', selected_key, course_id)
if selected ~= 1 then
    return "0"
end

redis.call('SREM', selected_key, course_id)
redis.call('INCR', stock_key)

local waiters = redis.call('ZRANGE', waiting_key, 0, 0)
if #waiters > 0 then
    local waiter = waiters[1]
    redis.call('DECR', stock_key)
    redis.call('SADD', 'course:selected:' .. waiter, course_id)
    redis.call('ZREM', waiting_key, waiter)
    redis.call('DECR', waiting_count_key)
    return waiter
end
return "1"