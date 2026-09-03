package com.messaging.auth.dto;

public record ResetPasswordRequest(
        String resetToken,
        String password,
        String confirmPassword
) {
}
