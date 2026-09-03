package com.messaging.session.service;

import com.messaging.common.exception.UnauthorizedException;
import com.messaging.security.jwt.JwtProperties;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.entity.UserSession;
import com.messaging.session.repository.UserSessionRepository;
import com.messaging.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

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
        session.setRefreshToken(hash(refreshToken));
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
        UserSession session = userSessionRepository.findByUserAndRefreshTokenAndActiveTrue(user, hash(currentRefreshToken))
                .filter(existingSession -> existingSession.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        session.setRefreshToken(hash(nextRefreshToken));
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
        userSessionRepository.findByRefreshTokenAndActiveTrue(hash(refreshToken))
                .ifPresent(session -> {
                    session.setActive(false);
                    session.setRefreshToken(hash(refreshToken));
                    userSessionRepository.save(session);
                });
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash refresh token", exception);
        }
    }
}
