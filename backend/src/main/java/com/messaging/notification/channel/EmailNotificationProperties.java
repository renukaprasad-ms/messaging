package com.messaging.notification.channel;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.notification.email")
public class EmailNotificationProperties {

    private String from;
    private String otpSubject = "Your verification code";
}
