package com.messaging.auth.dto;

public record LoginRequest(
        String identifier,
        String password,
        boolean rememberMe
) {
}
