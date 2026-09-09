if redis.call('HGET', KEYS[1], 'salt') ~= ARGV[1] then return -1 end
local attempts = redis.call('HINCRBY', KEYS[1], 'attempts', 1)
if attempts > tonumber(ARGV[3]) then return -2 end
if redis.call('HGET', KEYS[1], 'hash') ~= ARGV[2] then return 0 end
redis.call('DEL', KEYS[1])
return 1
