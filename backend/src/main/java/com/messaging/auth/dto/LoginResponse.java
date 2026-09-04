package com.messaging.auth.dto;

import com.messaging.user.entity.UserStatus;

public record LoginResponse(
                String name,
                String email,
                String phone,
                boolean hasCompany,
                UserStatus status) {
}
