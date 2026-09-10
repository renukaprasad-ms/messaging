package com.messaging.subscription.dto;

import com.messaging.user.entity.AccountType;
import java.math.BigDecimal;
import java.util.List;

public record SubscriptionPlanResponse(
    Long id,
    String code,
    AccountType accountType,
    String name,
    BigDecimal monthlyPrice,
    String currency,
    boolean active,
    List<PlanLimitResponse> limits,
    List<ChannelPricingResponse> pricing) {}
