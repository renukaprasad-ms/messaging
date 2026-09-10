package com.messaging.subscription.dto;

import java.math.BigDecimal;

public record PlanLimitResponse(String featureKey, BigDecimal limitValue, String unit) {}
