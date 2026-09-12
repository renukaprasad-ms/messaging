package com.messaging.kafka.event;

public record OtpRequestedEvent(String identifier, String deliveryChannel) {}
