package com.messaging.session.service;

import com.messaging.security.jwt.JwtProperties;
import com.messaging.session.entity.SessionPlatform;
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

    public UserSession createOrUpdateSession(User user, SessionPlatform platform, String refreshToken) {
        Instant now = Instant.now();
        UserSession session = userSessionRepository.findByUserAndPlatform(user, platform)
                .orElseGet(UserSession::new);

        session.setUser(user);
        session.setPlatform(platform);
        session.setRefreshToken(hash(refreshToken));
        session.setActive(true);
        session.setLastActiveAt(now);
        session.setExpiresAt(now.plus(jwtProperties.getRefreshExpiration()));

        return userSessionRepository.save(session);
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
