package com.messaging.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PlanLimitRequest(
    @NotBlank @Size(max = 100) String featureKey,
    @NotNull @DecimalMin("0.00") BigDecimal limitValue,
    @NotBlank @Size(max = 50) String unit) {}
