package com.messaging.user.dto;

public record UserResponse(
        String userName,
        String email,
        String phone
) {
}
