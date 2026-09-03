package com.messaging.auth.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth.password-reset")
public class PasswordResetProperties {

    private Duration tokenTtl = Duration.ofMinutes(10);
}
