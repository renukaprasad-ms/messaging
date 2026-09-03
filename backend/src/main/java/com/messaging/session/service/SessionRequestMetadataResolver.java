package com.messaging.session.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.entity.SessionPlatform;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class SessionRequestMetadataResolver {

    private static final String PLATFORM_HEADER = "X-Platform";
    private static final String DEVICE_ID_HEADER = "X-Device-Id";
    private static final String DEVICE_NAME_HEADER = "X-Device-Name";

    public SessionRequestMetadata resolve(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        return new SessionRequestMetadata(
                platform(request.getHeader(PLATFORM_HEADER), userAgent),
                request.getHeader(DEVICE_ID_HEADER),
                request.getHeader(DEVICE_NAME_HEADER),
                ipAddress(request),
                userAgent
        );
    }

    private SessionPlatform platform(String platformHeader, String userAgent) {
        if (platformHeader != null && !platformHeader.isBlank()) {
            try {
                return SessionPlatform.valueOf(platformHeader.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new BadRequestException("Invalid platform header");
            }
        }

        String normalizedUserAgent = userAgent == null ? "" : userAgent.toLowerCase();
        if (normalizedUserAgent.contains("android")) {
            return SessionPlatform.ANDROID;
        }
        if (normalizedUserAgent.contains("iphone") || normalizedUserAgent.contains("ipad") || normalizedUserAgent.contains("ios")) {
            return SessionPlatform.IOS;
        }
        return SessionPlatform.WEB;
    }

    private String ipAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
