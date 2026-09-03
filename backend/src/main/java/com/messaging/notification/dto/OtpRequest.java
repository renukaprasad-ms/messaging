package com.messaging.notification.dto;

import com.messaging.notification.model.NotificationChannel;
import lombok.Getter;

@Getter
public class OtpRequest {

    private String destination;
    private NotificationChannel channel;
}
