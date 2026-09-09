package com.messaging.session.service;

import com.messaging.common.exception.UnauthorizedException;
import com.messaging.common.util.HashUtils;
import com.messaging.security.jwt.JwtProperties;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.entity.UserSession;
import com.messaging.session.repository.UserSessionRepository;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSessionService {

  private final UserSessionRepository userSessionRepository;
  private final JwtProperties jwtProperties;
  private final UserService userService;

  private void lockUser(User user) {
    userService.getForUpdate(user.getId());
  }

  public UserSession createOrUpdateSession(
      User user, String refreshToken, SessionRequestMetadata metadata) {
    lockUser(user);
    Instant now = Instant.now();
    UserSession session =
        userSessionRepository
            .findByUserAndPlatform(user, metadata.platform())
            .orElseGet(UserSession::new);

    session.setUser(user);
    session.setAccessKey(UUID.randomUUID().toString());
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

  public UserSession rotateRefreshToken(
      User user,
      String currentRefreshToken,
      String nextRefreshToken,
      SessionRequestMetadata metadata) {
    lockUser(user);
    Instant now = Instant.now();
    UserSession session =
        userSessionRepository
            .findByUserAndRefreshTokenAndActiveTrue(user, HashUtils.sha256Hex(currentRefreshToken))
            .filter(existingSession -> existingSession.getExpiresAt().isAfter(now))
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

    session.setRefreshToken(HashUtils.sha256Hex(nextRefreshToken));
    session.setDeviceId(metadata.deviceId());
    session.setDeviceName(metadata.deviceName());
    session.setIpAddress(metadata.ipAddress());
    session.setUserAgent(metadata.userAgent());
    session.setLastActiveAt(now);
    session.setExpiresAt(now.plus(jwtProperties.getRefreshExpiration()));

    return userSessionRepository.save(session);
  }

  public void revokeRefreshToken(String refreshToken) {
    userSessionRepository
        .findByRefreshTokenAndActiveTrue(HashUtils.sha256Hex(refreshToken))
        .ifPresent(
            found -> {
              lockUser(found.getUser());
              userSessionRepository.revokeById(found.getId());
            });
  }

  public void revokeAll(User user) {
    lockUser(user);
    userSessionRepository.revokeByUserId(user.getId());
  }
}
