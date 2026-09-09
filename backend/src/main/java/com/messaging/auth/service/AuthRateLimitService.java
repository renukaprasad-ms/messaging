package com.messaging.auth.service;

import com.messaging.common.exception.TooManyRequestsException;
import com.messaging.common.util.HashUtils;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/** Limits expensive password checks. Each counter expires and contains no raw identifier. */
@Service
@RequiredArgsConstructor
public class AuthRateLimitService {
  private static final DefaultRedisScript<Long> COUNT =
      new DefaultRedisScript<>(
          """
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then redis.call('EXPIRE', KEYS[1], 60) end
            return count
            """,
          Long.class);
  private final StringRedisTemplate redis;

  public void checkLogin(String identifier, String ipAddress) {
    check("login-account", identifier.trim().toLowerCase(Locale.ROOT), 10);
    check("login-ip", ipAddress, 30);
  }

  private void check(String scope, String value, int limit) {
    Long attempts =
        redis.execute(COUNT, List.of("auth-limit:" + scope + ":" + HashUtils.sha256Hex(value)));
    if (attempts == null || attempts > limit)
      throw new TooManyRequestsException("Too many sign-in attempts. Try again in one minute.");
  }
}
