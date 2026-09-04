package com.messaging.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.queues")
public record KafkaQueueProperties(
        QueueProperties critical,
        QueueProperties high,
        QueueProperties medium,
        QueueProperties low
) {
}
