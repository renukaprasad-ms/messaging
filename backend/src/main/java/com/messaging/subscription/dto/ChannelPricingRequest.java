package com.messaging.subscription.dto;

import com.messaging.subscription.entity.BillingChannel;
import com.messaging.subscription.entity.MessageType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ChannelPricingRequest(
    @NotNull BillingChannel channel,
    @NotNull MessageType messageType,
    @NotNull @DecimalMin("0.0000") BigDecimal pricePerSegment,
    @Min(0) @Max(1000000) int freeDailySegments,
    @NotBlank @Size(min = 3, max = 3) String currency,
    boolean active) {}
