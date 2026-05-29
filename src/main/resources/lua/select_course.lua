local stock_key = KEYS[1]
local selected_key = KEYS[2]
local waiting_key = KEYS[3]
local waiting_count_key = KEYS[4]
local course_id = ARGV[1]
local user_id = ARGV[2]
local timestamp = ARGV[3]
local max_waiting = tonumber(ARGV[4] or "3")

local selected = redis.call('SISMEMBER', selected_key, course_id)
if selected == 1 then
    return -2
end

local waiting_rank = redis.call('ZRANK', waiting_key, user_id)
if waiting_rank ~= false then
    return -3
end

local stock = tonumber(redis.call('GET', stock_key) or "0")
if stock <= 0 then
    local current_waiting = tonumber(redis.call('GET', waiting_count_key) or "0")
    if current_waiting >= max_waiting then
        return -4
    end
    redis.call('ZADD', waiting_key, timestamp, user_id)
    redis.call('INCR', waiting_count_key)
    return -1
end

redis.call('DECR', stock_key)
redis.call('SADD', selected_key, course_id)
return 1