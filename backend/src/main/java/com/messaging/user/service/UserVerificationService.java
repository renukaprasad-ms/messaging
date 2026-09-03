package com.messaging.user.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.notification.model.NotificationChannel;
import com.messaging.notification.service.OtpService;
import com.messaging.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final UserService userService;
    private final OtpService otpService;

    public void sendEmailVerificationOtp(Long userId) {
        User user = userService.getById(userId);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("User email is not available");
        }
        otpService.sendOtp(user.getEmail(), NotificationChannel.EMAIL);
    }

    public void sendPhoneVerificationOtp(Long userId) {
        User user = userService.getById(userId);
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new BadRequestException("User phone is not available");
        }
        otpService.sendOtp(user.getPhone(), NotificationChannel.SMS);
    }

    public void verifyEmail(Long userId, String otp) {
        User user = userService.getById(userId);
        if (!otpService.verifyOtp(user.getEmail(), NotificationChannel.EMAIL, otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        user.setEmailVerified(true);
        userService.save(user);
    }

    public void verifyPhone(Long userId, String otp) {
        User user = userService.getById(userId);
        if (!otpService.verifyOtp(user.getPhone(), NotificationChannel.SMS, otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        user.setPhoneVerified(true);
        userService.save(user);
    }
}
