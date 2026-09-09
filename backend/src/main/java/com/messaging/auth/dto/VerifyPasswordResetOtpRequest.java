package com.messaging.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyPasswordResetOtpRequest(
    @NotBlank @Size(max = 320) String identifier,
    @NotBlank @Pattern(regexp = "[0-9]{4,8}") String otp) {}
