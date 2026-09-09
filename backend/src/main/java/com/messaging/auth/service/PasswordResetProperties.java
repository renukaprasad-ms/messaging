package com.messaging.auth.service;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth.password-reset")
public class PasswordResetProperties {

  private Duration tokenTtl = Duration.ofMinutes(10);
}
