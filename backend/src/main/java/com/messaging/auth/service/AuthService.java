package com.messaging.auth.service;

import com.messaging.auth.dto.RegisterRequest;
import com.messaging.auth.dto.RegisterResponse;
import com.messaging.security.jwt.TokenPair;
import com.messaging.session.dto.CreateOrUpdateSessionRequest;
import com.messaging.session.entity.SessionPlatform;
import com.messaging.session.service.UserSessionService;
import com.messaging.user.dto.CreateUserRequest;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final int MAX_IP_LENGTH = 45;
  private static final int MAX_USER_AGENT_LENGTH = 512;

  private final UserService userService;
  private final UserSessionService userSessionService;
  private final AuthTokenService authTokenService;

  @Transactional
  public RegisterResponse register(
      RegisterRequest request, HttpServletRequest servletRequest, HttpHeaders headers) {
    User user =
        userService.createUser(
            new CreateUserRequest(
                request.email(), request.name(), null, request.username(), request.password(), false));

    TokenPair tokenPair =
        authTokenService.createTokens(
            user.getId().toString(), Map.of("username", user.getUsername(), "email", user.getEmail()));

    userSessionService.createOrUpdate(
        new CreateOrUpdateSessionRequest(
            user,
            SessionPlatform.WEB,
            authTokenService.refreshTokenId(tokenPair.refreshToken()),
            authTokenService.refreshTokenExpiresAt(tokenPair.refreshToken()),
            clientIp(servletRequest),
            truncate(servletRequest.getHeader(HttpHeaders.USER_AGENT), MAX_USER_AGENT_LENGTH),
            "Web"));

    authTokenService.addTokenCookies(headers, tokenPair);

    return new RegisterResponse(user.getUsername(), user.getEmail());
  }

  private String clientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return truncate(forwardedFor.split(",", 2)[0].trim(), MAX_IP_LENGTH);
    }
    return truncate(request.getRemoteAddr(), MAX_IP_LENGTH);
  }

  private String truncate(String value, int maxLength) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
  }
}
