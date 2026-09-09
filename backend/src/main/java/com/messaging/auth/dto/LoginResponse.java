package com.messaging.auth.dto;

import com.messaging.user.entity.UserStatus;
import java.util.Set;

public record LoginResponse(
    String name,
    String email,
    String phone,
    boolean hasCompany,
    UserStatus status,
    Set<String> platformRoles,
    boolean passwordChangeRequired) {}
