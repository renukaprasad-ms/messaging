package com.messaging.config;

import com.messaging.notification.channel.EmailNotificationProperties;
import com.messaging.notification.service.OtpProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EmailNotificationProperties.class, OtpProperties.class})
public class NotificationConfig {
}
