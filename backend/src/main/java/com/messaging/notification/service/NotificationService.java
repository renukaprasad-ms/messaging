package com.messaging.notification.service;

import com.messaging.notification.channel.SmsNotificationSender;
import com.messaging.notification.dto.NotificationQueueMessage;
import com.messaging.notification.model.NotificationChannel;
import com.messaging.notification.model.NotificationQueuePriority;
import com.messaging.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SmsNotificationSender smsSender;
    private final NotificationQueueProducer queueProducer;

    public void sendOtp(String destination, NotificationChannel channel, String otp) {
        switch (channel) {
            case EMAIL -> queueProducer.publish(
                    priority(NotificationType.OTP),
                    NotificationQueueMessage.create(
                            NotificationType.OTP,
                            NotificationChannel.EMAIL,
                            destination,
                            Map.of("otp", otp)));
            case SMS -> smsSender.sendOtp(destination, otp);
        }
    }

    private NotificationQueuePriority priority(NotificationType type) {
        return switch (type) {
            case OTP -> NotificationQueuePriority.CRITICAL;
            case PASSWORD_RESET, LOGIN_ALERT -> NotificationQueuePriority.HIGH;
            case EMAIL_VERIFICATION -> NotificationQueuePriority.MEDIUM;
            case SYSTEM_NOTIFICATION -> NotificationQueuePriority.LOW;
        };
    }
}
