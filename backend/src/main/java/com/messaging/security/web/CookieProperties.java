package com.messaging.security.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.cookie")
public class CookieProperties {

    private String accessName;
    private String refreshName;
    private Duration accessMaxAge;
    private Duration refreshMaxAge;
    private String path;
    private String domain;
    private boolean httpOnly;
    private boolean secure;
    private String sameSite;
}
