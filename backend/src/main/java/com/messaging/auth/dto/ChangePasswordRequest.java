package com.messaging.auth.dto;

import com.messaging.security.password.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank @Size(max = 72) String currentPassword,
    @StrongPassword String password,
    @NotBlank @Size(max = 72) String confirmPassword) {}
