package com.messaging.user.dto;

import com.messaging.user.entity.UserStatus;

public record UserSummary(Long id, String name, String email, UserStatus status) {}
