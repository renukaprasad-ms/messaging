package com.messaging.config.datasource;

public record DataSourceConnectionProperties(
        String jdbcUrl,
        String username,
        String password,
        int maximumPoolSize,
        int minimumIdle
) {
}
