package com.messaging.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource")
public record ReadWriteDataSourceProperties(
        DataSourceConnectionProperties write,
        DataSourceConnectionProperties read
) {
}
