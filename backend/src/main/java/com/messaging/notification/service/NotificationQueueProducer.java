package com.messaging.notification.service;

import com.messaging.config.kafka.KafkaQueueProperties;
import com.messaging.config.kafka.QueueProperties;
import com.messaging.notification.dto.NotificationQueueMessage;
import com.messaging.notification.model.NotificationQueuePriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueueProducer {

    private final KafkaTemplate<String, NotificationQueueMessage> kafkaTemplate;
    private final KafkaQueueProperties queueProperties;

    public void publish(NotificationQueuePriority priority, NotificationQueueMessage message) {
        String topic = topic(priority);
        kafkaTemplate.send(topic, message.id(), message)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish notification {} to {}", message.id(), topic, error);
                    }
                });
    }

    private String topic(NotificationQueuePriority priority) {
        QueueProperties queue = switch (priority) {
            case CRITICAL -> queueProperties.critical();
            case HIGH -> queueProperties.high();
            case MEDIUM -> queueProperties.medium();
            case LOW -> queueProperties.low();
        };

        return queue.topic();
    }
}
