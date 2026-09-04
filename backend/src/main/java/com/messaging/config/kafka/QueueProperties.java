package com.messaging.config.kafka;

public record QueueProperties(
        String topic,
        int partitions,
        short replicas
) {
}
