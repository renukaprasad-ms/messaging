package com.messaging.user.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.user.dto.OtpVerifyRequest;
import com.messaging.user.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/verification")
@RequiredArgsConstructor
public class UserVerificationController {

    private final UserVerificationService userVerificationService;

    @PostMapping("/email")
    public ApiResponse<Void> sendEmailVerificationOtp(@AuthenticationPrincipal String userId) {
        userVerificationService.sendEmailVerificationOtp(Long.valueOf(userId));
        return ApiResponse.success(HttpStatus.OK.value(), "Email verification OTP sent");
    }

    @PostMapping("/phone")
    public ApiResponse<Void> sendPhoneVerificationOtp(@AuthenticationPrincipal String userId) {
        userVerificationService.sendPhoneVerificationOtp(Long.valueOf(userId));
        return ApiResponse.success(HttpStatus.OK.value(), "Phone verification OTP sent");
    }

    @PostMapping("/email/resend")
    public ApiResponse<Void> resendEmailVerificationOtp(@AuthenticationPrincipal String userId) {
        userVerificationService.sendEmailVerificationOtp(Long.valueOf(userId));
        return ApiResponse.success(HttpStatus.OK.value(), "Email verification OTP sent");
    }

    @PostMapping("/phone/resend")
    public ApiResponse<Void> resendPhoneVerificationOtp(@AuthenticationPrincipal String userId) {
        userVerificationService.sendPhoneVerificationOtp(Long.valueOf(userId));
        return ApiResponse.success(HttpStatus.OK.value(), "Phone verification OTP sent");
    }

    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(
            @AuthenticationPrincipal String userId,
            @RequestBody OtpVerifyRequest request) {
        userVerificationService.verifyEmail(Long.valueOf(userId), request.otp());
        return ApiResponse.success(HttpStatus.OK.value(), "Email verified successfully");
    }

    @PostMapping("/phone/verify")
    public ApiResponse<Void> verifyPhone(
            @AuthenticationPrincipal String userId,
            @RequestBody OtpVerifyRequest request) {
        userVerificationService.verifyPhone(Long.valueOf(userId), request.otp());
        return ApiResponse.success(HttpStatus.OK.value(), "Phone verified successfully");
    }
}
