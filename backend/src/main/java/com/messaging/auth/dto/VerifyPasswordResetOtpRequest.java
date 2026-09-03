package com.messaging.auth.dto;

public record VerifyPasswordResetOtpRequest(
        String identifier,
        String otp
) {
}
