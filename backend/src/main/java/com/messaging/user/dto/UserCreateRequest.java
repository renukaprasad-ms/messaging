package com.messaging.user.dto;

import com.messaging.security.password.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Email @Size(max = 320) String email,
    @StrongPassword String password,
    @NotBlank @Size(max = 72) String confirmPassword,
    @Size(max = 30) String phone) {}
