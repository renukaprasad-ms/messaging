package com.messaging.notification.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.common.exception.TooManyRequestsException;
import com.messaging.common.util.HashUtils;
import com.messaging.notification.model.NotificationChannel;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {
  public enum Purpose {
    EMAIL_VERIFICATION,
    PHONE_VERIFICATION,
    PASSWORD_RESET
  }

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final DefaultRedisScript<Long> ISSUE = script("issue-otp.lua");
  private static final DefaultRedisScript<Long> VERIFY = script("verify-otp.lua");
  private static final DefaultRedisScript<Long> CANCEL = script("cancel-otp.lua");

  private final NotificationService notificationService;
  private final StringRedisTemplate redisTemplate;
  private final OtpProperties properties;

  public void sendOtp(String destination, NotificationChannel channel, Purpose purpose) {
    List<String> keys = keys(destination, channel, purpose);
    String otp = generateOtp();
    String salt = UUID.randomUUID().toString();
    Long issued =
        redisTemplate.execute(
            ISSUE,
            keys,
            salt,
            HashUtils.sha256Hex(salt + ":" + otp),
            Long.toString(properties.getTtl().toMillis()),
            Long.toString(properties.getResendCooldown().toMillis()));
    if (!Long.valueOf(1).equals(issued)) {
      throw new TooManyRequestsException("Please wait before requesting another OTP");
    }
    try {
      notificationService.sendOtp(destination, channel, otp);
    } catch (RuntimeException error) {
      // Only cancel this issuance; never erase a newer code from another request.
      redisTemplate.execute(CANCEL, keys, salt);
      throw error;
    }
  }

  public boolean verifyOtp(
      String destination, NotificationChannel channel, Purpose purpose, String otp) {
    List<String> keys = keys(destination, channel, purpose);
    if (otp == null || !otp.matches("[0-9]{" + properties.getLength() + "}")) {
      throw new BadRequestException("Enter a valid verification code");
    }
    Object salt = redisTemplate.opsForHash().get(keys.getFirst(), "salt");
    if (salt == null) throw new BadRequestException("OTP is expired or invalid");
    Long result =
        redisTemplate.execute(
            VERIFY,
            keys,
            salt.toString(),
            HashUtils.sha256Hex(salt + ":" + otp),
            Integer.toString(properties.getMaxAttempts()));
    if (Long.valueOf(-2).equals(result))
      throw new TooManyRequestsException("Too many OTP attempts");
    if (Long.valueOf(-1).equals(result)) throw new BadRequestException("OTP is expired or invalid");
    return Long.valueOf(1).equals(result);
  }

  private String generateOtp() {
    if (properties.getLength() < 4 || properties.getLength() > 8) {
      throw new IllegalStateException("OTP length must be between 4 and 8");
    }
    int bound = (int) Math.pow(10, properties.getLength());
    return String.format(Locale.ROOT, "%0" + properties.getLength() + "d", RANDOM.nextInt(bound));
  }

  private List<String> keys(String destination, NotificationChannel channel, Purpose purpose) {
    if (destination == null || destination.isBlank() || channel == null || purpose == null) {
      throw new BadRequestException("OTP destination, channel and purpose are required");
    }
    String identity = purpose + ":" + channel + ":" + destination.trim().toLowerCase(Locale.ROOT);
    // Both keys share a Redis Cluster slot without exposing contact details.
    String base = "otp:{" + HashUtils.sha256Hex(identity) + "}";
    return List.of(base, base + ":cooldown");
  }

  private static DefaultRedisScript<Long> script(String name) {
    var script = new DefaultRedisScript<Long>();
    script.setLocation(new ClassPathResource("redis/" + name));
    script.setResultType(Long.class);
    return script;
  }
}
