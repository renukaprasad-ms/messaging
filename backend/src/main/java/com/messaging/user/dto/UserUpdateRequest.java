package com.messaging.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 30) String phone,
    @Size(max = 1000) String profilePhotoUrl) {}
