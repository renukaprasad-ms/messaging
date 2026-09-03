package com.messaging.notification.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.common.exception.TooManyRequestsException;
import com.messaging.common.util.HashUtils;
import com.messaging.notification.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;
    private final OtpProperties properties;

    public void sendOtp(String destination, NotificationChannel channel) {
        validateRequest(destination, channel);

        String cooldownKey = cooldownKey(destination, channel);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new TooManyRequestsException("Please wait before requesting another OTP");
        }

        String otp = generateOtp();
        String salt = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(otpKey(destination, channel), HashUtils.sha256Hex(salt + ":" + otp),
                properties.getTtl());
        redisTemplate.opsForValue().set(saltKey(destination, channel), salt, properties.getTtl());
        redisTemplate.delete(attemptsKey(destination, channel));
        redisTemplate.opsForValue().set(cooldownKey, "1", properties.getResendCooldown());

        notificationService.sendOtp(destination, channel, otp);
    }

    public boolean verifyOtp(String destination, NotificationChannel channel, String otp) {
        validateRequest(destination, channel);
        if (otp == null || otp.isBlank()) {
            throw new BadRequestException("OTP is required");
        }

        String attemptsKey = attemptsKey(destination, channel);
        long attempts = incrementAttempts(attemptsKey);
        if (attempts > properties.getMaxAttempts()) {
            clearOtp(destination, channel);
            throw new TooManyRequestsException("Too many OTP attempts");
        }

        String salt = redisTemplate.opsForValue().get(saltKey(destination, channel));
        String expectedHash = redisTemplate.opsForValue().get(otpKey(destination, channel));
        if (salt == null || expectedHash == null) {
            throw new BadRequestException("OTP is expired or invalid");
        }

        boolean valid = MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                HashUtils.sha256Hex(salt + ":" + otp).getBytes(StandardCharsets.UTF_8));

        if (valid) {
            clearOtp(destination, channel);
        }

        return valid;
    }

    private long incrementAttempts(String key) {
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, properties.getTtl());
        }
        return attempts == null ? 1 : attempts;
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, properties.getLength());
        int floor = bound / 10;
        return String.valueOf(floor + RANDOM.nextInt(bound - floor));
    }

    private void validateRequest(String destination, NotificationChannel channel) {
        if (destination == null || destination.isBlank()) {
            throw new BadRequestException("OTP destination is required");
        }
        if (channel == null) {
            throw new BadRequestException("OTP channel is required");
        }
    }

    private void clearOtp(String destination, NotificationChannel channel) {
        redisTemplate.delete(otpKey(destination, channel));
        redisTemplate.delete(saltKey(destination, channel));
        redisTemplate.delete(attemptsKey(destination, channel));
        redisTemplate.delete(cooldownKey(destination, channel));
    }

    private String otpKey(String destination, NotificationChannel channel) {
        return baseKey("otp", destination, channel);
    }

    private String saltKey(String destination, NotificationChannel channel) {
        return baseKey("otp:salt", destination, channel);
    }

    private String attemptsKey(String destination, NotificationChannel channel) {
        return baseKey("otp:attempts", destination, channel);
    }

    private String cooldownKey(String destination, NotificationChannel channel) {
        return baseKey("otp:cooldown", destination, channel);
    }

    private String baseKey(String prefix, String destination, NotificationChannel channel) {
        return prefix + ":" + channel.name().toLowerCase() + ":" + destination.trim().toLowerCase();
    }
}
