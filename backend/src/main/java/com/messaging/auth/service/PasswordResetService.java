package com.messaging.auth.service;

import com.messaging.auth.dto.ForgotPasswordRequest;
import com.messaging.auth.dto.ResetPasswordRequest;
import com.messaging.auth.dto.VerifyPasswordResetOtpRequest;
import com.messaging.auth.dto.VerifyPasswordResetOtpResponse;
import com.messaging.common.exception.BadRequestException;
import com.messaging.common.util.HashUtils;
import com.messaging.notification.model.NotificationChannel;
import com.messaging.notification.service.OtpService;
import com.messaging.session.service.UserSessionService;
import com.messaging.user.entity.User;
import com.messaging.user.repository.UserRepository;
import com.messaging.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserSessionService userSessionService;
    private final OtpService otpService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties properties;

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = user(request.identifier())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String destination = destination(user, request.identifier());
        NotificationChannel channel = channel(user, request.identifier());
        otpService.sendOtp(destination, channel);
    }

    public VerifyPasswordResetOtpResponse verifyOtp(VerifyPasswordResetOtpRequest request) {
        User user = user(request.identifier())
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        String destination = destination(user, request.identifier());
        NotificationChannel channel = channel(user, request.identifier());

        if (!otpService.verifyOtp(destination, channel, request.otp())) {
            throw new BadRequestException("Invalid OTP");
        }

        String resetToken = resetToken();
        redisTemplate.opsForValue().set(resetTokenKey(resetToken), user.getId().toString(), properties.getTokenTtl());
        return new VerifyPasswordResetOtpResponse(resetToken);
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (request.password() == null || !request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String key = resetTokenKey(request.resetToken());
        String userId = redisTemplate.opsForValue().get(key);
        if (userId == null) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = userService.getById(Long.valueOf(userId));
        user.setPassword(passwordEncoder.encode(request.password()));
        userService.save(user);
        userSessionService.revokeAll(user);
        redisTemplate.delete(key);
    }

    private Optional<User> user(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmailOrPhone(identifier, identifier);
    }

    private String destination(User user, String identifier) {
        if (identifier.equalsIgnoreCase(user.getEmail())) {
            return user.getEmail();
        }
        return user.getPhone();
    }

    private NotificationChannel channel(User user, String identifier) {
        if (identifier.equalsIgnoreCase(user.getEmail())) {
            return NotificationChannel.EMAIL;
        }
        return NotificationChannel.SMS;
    }

    private String resetToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String resetTokenKey(String resetToken) {
        if (resetToken == null || resetToken.isBlank()) {
            throw new BadRequestException("Reset token is required");
        }
        return "password-reset:token:" + HashUtils.sha256Hex(resetToken);
    }
}
