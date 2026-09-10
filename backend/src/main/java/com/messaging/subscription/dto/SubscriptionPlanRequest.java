package com.messaging.subscription.dto;

import com.messaging.user.entity.AccountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record SubscriptionPlanRequest(
    @NotBlank @Size(max = 50) String code,
    @NotNull AccountType accountType,
    @NotBlank @Size(max = 100) String name,
    @NotNull @DecimalMin("0.00") BigDecimal monthlyPrice,
    @NotBlank @Size(min = 3, max = 3) String currency,
    boolean active,
    @Valid List<PlanLimitRequest> limits,
    @Valid List<ChannelPricingRequest> pricing) {}
