package com.messaging.auth.dto;

import com.messaging.user.entity.AccountType;
import com.messaging.user.entity.UserStatus;
import java.util.Set;

public record LoginResponse(
    String name,
    String email,
    String phone,
    String profilePhotoUrl,
    AccountType accountType,
    boolean hasCompany,
    UserStatus status,
    Set<String> platformRoles,
    boolean passwordChangeRequired) {}
