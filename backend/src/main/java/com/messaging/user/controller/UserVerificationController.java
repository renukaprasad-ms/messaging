package com.messaging.user.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.user.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/verification")
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
}
