package com.messaging.auth.dto;

public record LoginResult(
        LoginResponse user,
        String accessToken,
        String refreshToken
) {
}
