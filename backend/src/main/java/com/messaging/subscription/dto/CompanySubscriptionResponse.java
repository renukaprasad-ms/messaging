package com.messaging.subscription.dto;

import com.messaging.subscription.entity.SubscriptionStatus;
import java.time.Instant;

public record CompanySubscriptionResponse(
    Long id,
    Long companyId,
    SubscriptionStatus status,
    Instant startsAt,
    Instant endsAt,
    Instant renewsAt,
    SubscriptionPlanResponse plan) {}
