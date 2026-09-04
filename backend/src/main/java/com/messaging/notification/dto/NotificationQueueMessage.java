package com.messaging.notification.dto;

import com.messaging.notification.model.NotificationChannel;
import com.messaging.notification.model.NotificationType;

import java.util.Map;
import java.util.UUID;

public record NotificationQueueMessage(
        String id,
        NotificationType type,
        NotificationChannel channel,
        String destination,
        Map<String, String> payload,
        long createdAt
) {

    public static NotificationQueueMessage create(
            NotificationType type,
            NotificationChannel channel,
            String destination,
            Map<String, String> payload
    ) {
        return new NotificationQueueMessage(
                UUID.randomUUID().toString(),
                type,
                channel,
                destination,
                payload,
                System.currentTimeMillis());
    }
}
