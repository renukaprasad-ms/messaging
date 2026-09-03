package com.messaging.auth.dto;

public record LoginResponse(
        String name,
        String email,
        String phone
) {
}
