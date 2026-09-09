package com.messaging.auth.service;

import com.messaging.auth.dto.ChangePasswordRequest;
import com.messaging.auth.dto.ForgotPasswordRequest;
import com.messaging.auth.dto.ResetPasswordRequest;
import com.messaging.auth.dto.VerifyPasswordResetOtpRequest;
import com.messaging.auth.dto.VerifyPasswordResetOtpResponse;
import com.messaging.common.exception.BadRequestException;
import com.messaging.common.exception.TooManyRequestsException;
import com.messaging.common.util.HashUtils;
import com.messaging.notification.model.NotificationChannel;
import com.messaging.notification.service.OtpService;
import com.messaging.security.AccessPolicy;
import com.messaging.session.service.UserSessionService;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final UserService userService;
  private final UserSessionService userSessionService;
  private final OtpService otpService;
  private final StringRedisTemplate redisTemplate;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetProperties properties;

  public void forgotPassword(ForgotPasswordRequest request) {
    User user = user(request.identifier()).orElse(null);
    if (user == null || !AccessPolicy.canSignIn(user)) return;

    String destination = destination(user, request.identifier());
    NotificationChannel channel = channel(user, request.identifier());
    try {
      otpService.sendOtp(destination, channel, OtpService.Purpose.PASSWORD_RESET);
    } catch (TooManyRequestsException ignored) {
      // Repeated requests keep the same public response so account existence is not disclosed.
    }
  }

  public VerifyPasswordResetOtpResponse verifyOtp(VerifyPasswordResetOtpRequest request) {
    User user =
        user(request.identifier()).orElseThrow(() -> new BadRequestException("Invalid OTP"));

    String destination = destination(user, request.identifier());
    NotificationChannel channel = channel(user, request.identifier());

    if (!otpService.verifyOtp(
        destination, channel, OtpService.Purpose.PASSWORD_RESET, request.otp())) {
      throw new BadRequestException("Invalid OTP");
    }

    String resetToken = resetToken();
    redisTemplate
        .opsForValue()
        .set(resetTokenKey(resetToken), user.getId().toString(), properties.getTokenTtl());
    return new VerifyPasswordResetOtpResponse(resetToken);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    if (request.password() == null || !request.password().equals(request.confirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }

    String key = resetTokenKey(request.resetToken());
    String userId = redisTemplate.opsForValue().getAndDelete(key);
    if (userId == null) {
      throw new BadRequestException("Invalid or expired reset token");
    }

    User user = userService.getForUpdate(Long.valueOf(userId));
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setPasswordChangeRequired(false);
    userService.save(user);
    userSessionService.revokeAll(user);
  }

  private Optional<User> user(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      return Optional.empty();
    }
    return userService.findByIdentifier(identifier);
  }

  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = userService.getForUpdate(userId);
    if (request.currentPassword().getBytes(StandardCharsets.UTF_8).length > 72
        || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new BadRequestException("Current password is incorrect");
    }
    if (!request.password().equals(request.confirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }
    if (passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BadRequestException("Choose a different password");
    }
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setPasswordChangeRequired(false);
    userService.save(user);
    userSessionService.revokeAll(user);
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
