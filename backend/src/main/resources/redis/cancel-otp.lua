if redis.call('HGET', KEYS[1], 'salt') ~= ARGV[1] then return 0 end
redis.call('DEL', KEYS[1], KEYS[2])
return 1
