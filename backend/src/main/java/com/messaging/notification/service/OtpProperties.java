package com.messaging.notification.service;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.notification.otp")
public class OtpProperties {

  private Duration ttl = Duration.ofMinutes(5);
  private Duration resendCooldown = Duration.ofMinutes(1);
  private int length = 6;
  private int maxAttempts = 5;
}
