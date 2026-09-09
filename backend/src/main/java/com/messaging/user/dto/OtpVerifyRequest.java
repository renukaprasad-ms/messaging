package com.messaging.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(@NotBlank @Pattern(regexp = "[0-9]{4,8}") String otp) {}
