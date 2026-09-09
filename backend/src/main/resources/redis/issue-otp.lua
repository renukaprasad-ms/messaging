if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end
redis.call('HSET', KEYS[1], 'salt', ARGV[1], 'hash', ARGV[2], 'attempts', 0)
redis.call('PEXPIRE', KEYS[1], ARGV[3])
redis.call('SET', KEYS[2], '1', 'PX', ARGV[4])
return 1
