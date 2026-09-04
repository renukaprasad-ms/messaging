package com.messaging.notification.service;

import com.messaging.notification.channel.EmailNotificationSender;
import com.messaging.notification.channel.SmsNotificationSender;
import com.messaging.notification.dto.NotificationQueueMessage;
import com.messaging.notification.model.NotificationChannel;
import com.messaging.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueueConsumer {

    private final EmailNotificationSender emailSender;
    private final SmsNotificationSender smsSender;

    @KafkaListener(
            topics = "${app.kafka.queues.critical.topic}",
            concurrency = "${app.kafka.queues.critical.concurrency}"
    )
    public void consumeCritical(NotificationQueueMessage message) {
        process(message);
    }

    @KafkaListener(
            topics = "${app.kafka.queues.high.topic}",
            concurrency = "${app.kafka.queues.high.concurrency}"
    )
    public void consumeHigh(NotificationQueueMessage message) {
        process(message);
    }

    @KafkaListener(
            topics = "${app.kafka.queues.medium.topic}",
            concurrency = "${app.kafka.queues.medium.concurrency}"
    )
    public void consumeMedium(NotificationQueueMessage message) {
        process(message);
    }

    @KafkaListener(
            topics = "${app.kafka.queues.low.topic}",
            concurrency = "${app.kafka.queues.low.concurrency}"
    )
    public void consumeLow(NotificationQueueMessage message) {
        process(message);
    }

    private void process(NotificationQueueMessage message) {
        if (message.type() == NotificationType.OTP) {
            sendOtp(message);
        }
    }

    private void sendOtp(NotificationQueueMessage message) {
        String otp = message.payload().get("otp");
        if (otp == null || otp.isBlank()) {
            log.warn("Skipping OTP notification {} because payload is missing otp", message.id());
            return;
        }

        if (message.channel() == NotificationChannel.EMAIL) {
            emailSender.sendOtp(message.destination(), otp);
            return;
        }

        smsSender.sendOtp(message.destination(), otp);
    }
}
