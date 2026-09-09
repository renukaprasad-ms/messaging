package com.messaging.notification.service;

import com.messaging.common.exception.ServiceUnavailableException;
import com.messaging.config.kafka.KafkaQueueProperties;
import com.messaging.config.kafka.QueueProperties;
import com.messaging.notification.dto.NotificationQueueMessage;
import com.messaging.notification.model.NotificationQueuePriority;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.notification.async", havingValue = "true")
@RequiredArgsConstructor
public class NotificationQueueProducer {

  private final KafkaTemplate<String, NotificationQueueMessage> kafkaTemplate;
  private final KafkaQueueProperties queueProperties;

  public void publish(NotificationQueuePriority priority, NotificationQueueMessage message) {
    String topic = topic(priority);
    try {
      kafkaTemplate.send(topic, message.id(), message).get(10, TimeUnit.SECONDS);
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      throw new ServiceUnavailableException("Notification delivery is unavailable");
    } catch (ExecutionException | TimeoutException | KafkaException error) {
      log.error("Failed to publish notification {} to {}", message.id(), topic);
      throw new ServiceUnavailableException("Notification delivery is unavailable");
    }
  }

  private String topic(NotificationQueuePriority priority) {
    QueueProperties queue =
        switch (priority) {
          case CRITICAL -> queueProperties.critical();
          case HIGH -> queueProperties.high();
          case MEDIUM -> queueProperties.medium();
          case LOW -> queueProperties.low();
        };

    return queue.topic();
  }
}
