package com.messaging.subscription.dto;

import com.messaging.subscription.entity.BillingChannel;
import com.messaging.subscription.entity.MessageType;
import java.math.BigDecimal;

public record ChannelPricingResponse(
    BillingChannel channel,
    MessageType messageType,
    BigDecimal pricePerSegment,
    int freeDailySegments,
    String currency,
    boolean active) {}
