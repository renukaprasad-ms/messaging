package com.messaging.kafka.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.messaging.kafka.priority.MessagePriority;
import org.junit.jupiter.api.Test;

class KafkaEventTests {

  @Test
  void createsEnvelopeWithRequiredMetadata() {
    EventMetadata metadata =
        EventMetadata.create(
            "auth.otp.requested",
            "v1",
            10L,
            20L,
            null,
            null,
            MessagePriority.CRITICAL);

    KafkaEvent<OtpRequestedEvent> event =
        KafkaEvent.of(metadata, new OtpRequestedEvent("user@example.com", "EMAIL"));

    assertThat(event.eventId()).isNotBlank();
    assertThat(event.eventType()).isEqualTo("auth.otp.requested");
    assertThat(event.eventVersion()).isEqualTo("v1");
    assertThat(event.occurredAt()).isNotNull();
    assertThat(event.priority()).isEqualTo(MessagePriority.CRITICAL);
    assertThat(event.correlationId()).isNotBlank();
    assertThat(event.payload()).isEqualTo(new OtpRequestedEvent("user@example.com", "EMAIL"));
  }
}
