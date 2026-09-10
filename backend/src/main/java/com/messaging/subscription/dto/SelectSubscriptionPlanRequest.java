package com.messaging.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SelectSubscriptionPlanRequest(@NotBlank @Size(max = 50) String planCode) {}
