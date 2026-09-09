package com.messaging.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.messaging.common.exception.ServiceUnavailableException;
import com.messaging.config.kafka.KafkaQueueProperties;
import com.messaging.config.kafka.QueueProperties;
import com.messaging.notification.dto.NotificationQueueMessage;
import com.messaging.notification.model.*;
import com.messaging.notification.service.NotificationQueueProducer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class NotificationQueueProducerTests {
  @Test
  @SuppressWarnings("unchecked")
  void brokerFailureIsReturnedToCaller() {
    KafkaTemplate<String, NotificationQueueMessage> template = mock(KafkaTemplate.class);
    var queue = new QueueProperties("test", 1, (short) 1, 1);
    var producer =
        new NotificationQueueProducer(
            template, new KafkaQueueProperties(queue, queue, queue, queue));
    var message =
        NotificationQueueMessage.create(
            NotificationType.OTP,
            NotificationChannel.EMAIL,
            "test@example.com",
            Map.of("otp", "123456"));
    when(template.send(anyString(), anyString(), any(NotificationQueueMessage.class)))
        .thenReturn(
            CompletableFuture.failedFuture(new IllegalStateException("Broker unavailable")));
    assertThrows(
        ServiceUnavailableException.class,
        () -> producer.publish(NotificationQueuePriority.CRITICAL, message));
  }
}
