package com.messaging.notification.service;

import com.messaging.notification.channel.EmailNotificationSender;
import com.messaging.notification.channel.SmsNotificationSender;
import com.messaging.notification.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailNotificationSender emailSender;
    private final SmsNotificationSender smsSender;

    public void sendOtp(String destination, NotificationChannel channel, String otp) {
        switch (channel) {
            case EMAIL -> emailSender.sendOtp(destination, otp);
            case SMS -> smsSender.sendOtp(destination, otp);
        }
    }
}
