package com.messaging.session.service;

import com.messaging.common.exception.UnauthorizedException;
import com.messaging.common.util.HashUtils;
import com.messaging.security.jwt.JwtProperties;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.entity.UserSession;
import com.messaging.session.repository.UserSessionRepository;
import com.messaging.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final JwtProperties jwtProperties;

    public UserSession createOrUpdateSession(User user, String refreshToken, SessionRequestMetadata metadata) {
        Instant now = Instant.now();
        UserSession session = userSessionRepository.findByUserAndPlatform(user, metadata.platform())
                .orElseGet(UserSession::new);

        session.setUser(user);
        session.setPlatform(metadata.platform());
        session.setRefreshToken(HashUtils.sha256Hex(refreshToken));
        session.setDeviceId(metadata.deviceId());
        session.setDeviceName(metadata.deviceName());
        session.setIpAddress(metadata.ipAddress());
        session.setUserAgent(metadata.userAgent());
        session.setActive(true);
        session.setLastActiveAt(now);
        session.setExpiresAt(now.plus(jwtProperties.getRefreshExpiration()));

        return userSessionRepository.save(session);
    }

    public UserSession rotateRefreshToken(User user, String currentRefreshToken, String nextRefreshToken, SessionRequestMetadata metadata) {
        Instant now = Instant.now();
        UserSession session = userSessionRepository.findByUserAndRefreshTokenAndActiveTrue(user, HashUtils.sha256Hex(currentRefreshToken))
                .filter(existingSession -> existingSession.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        session.setRefreshToken(HashUtils.sha256Hex(nextRefreshToken));
        session.setPlatform(metadata.platform());
        session.setDeviceId(metadata.deviceId());
        session.setDeviceName(metadata.deviceName());
        session.setIpAddress(metadata.ipAddress());
        session.setUserAgent(metadata.userAgent());
        session.setLastActiveAt(now);
        session.setExpiresAt(now.plus(jwtProperties.getRefreshExpiration()));

        return userSessionRepository.save(session);
    }

    public void revokeRefreshToken(String refreshToken) {
        userSessionRepository.findByRefreshTokenAndActiveTrue(HashUtils.sha256Hex(refreshToken))
                .ifPresent(session -> {
                    session.setActive(false);
                    session.setRefreshToken(HashUtils.sha256Hex(refreshToken));
                    userSessionRepository.save(session);
                });
    }

    public void revokeAll(User user) {
        userSessionRepository.findAllByUserAndActiveTrue(user)
                .forEach(session -> {
                    session.setActive(false);
                    userSessionRepository.save(session);
                });
    }

}
