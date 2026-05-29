-- save_chat.lua
-- 原子化保存AI对话记录到Redis，所有EXPIRE命令在Redis服务端执行，绕过DefaultedRedisConnection递归bug
-- KEYS[1]: sessionListKey  (ZSET) - 用户会话列表
-- KEYS[2]: sessionInfoKey  (HASH) - 会话元信息
-- KEYS[3]: chatKey         (LIST) - 对话消息列表
-- ARGV[1]: sessionId
-- ARGV[2]: now (timestamp millis)
-- ARGV[3]: userMsgJson
-- ARGV[4]: assistantMsgJson
-- ARGV[5]: title
-- ARGV[6]: userId (string)
-- ARGV[7]: ttl (seconds)
-- ARGV[8]: maxMessages
-- ARGV[9]: maxSessions

local sessionListKey = KEYS[1]
local sessionInfoKey = KEYS[2]
local chatKey = KEYS[3]

local sessionId = ARGV[1]
local now = ARGV[2]
local userMsgJson = ARGV[3]
local assistantMsgJson = ARGV[4]
local title = ARGV[5]
local userId = ARGV[6]
local ttl = tonumber(ARGV[7])
local maxMessages = tonumber(ARGV[8])
local maxSessions = tonumber(ARGV[9])

redis.call('ZADD', sessionListKey, now, sessionId)
redis.call('EXPIRE', sessionListKey, ttl)

redis.call('HSET', sessionInfoKey,
    'title', title,
    'userId', userId,
    'lastMessageAt', now,
    'createTime', now)
redis.call('HINCRBY', sessionInfoKey, 'messageCount', 2)
redis.call('EXPIRE', sessionInfoKey, ttl)

redis.call('RPUSH', chatKey, userMsgJson, assistantMsgJson)
redis.call('LTRIM', chatKey, -maxMessages, -1)
redis.call('EXPIRE', chatKey, ttl)

local count = redis.call('ZCARD', sessionListKey)
if count > maxSessions then
    redis.call('ZREMRANGEBYRANK', sessionListKey, 0, count - maxSessions - 1)
end

return 1