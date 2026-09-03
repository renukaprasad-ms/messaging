package com.messaging.session.dto;

import com.messaging.session.entity.SessionPlatform;

public record SessionRequestMetadata(
        SessionPlatform platform,
        String deviceId,
        String deviceName,
        String ipAddress,
        String userAgent
) {
}
