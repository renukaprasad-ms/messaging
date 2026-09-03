package com.messaging.user.dto;

public record UserCreateRequest(
        String name,
        String email,
        String password,
        String confirmPassword,
        String phone
) {
}
