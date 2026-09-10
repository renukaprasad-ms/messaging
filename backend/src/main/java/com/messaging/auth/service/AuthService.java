package com.messaging.auth.service;

import com.messaging.auth.dto.LoginRequest;
import com.messaging.auth.dto.LoginResult;
import com.messaging.common.exception.UnauthorizedException;
import com.messaging.security.AccessPolicy;
import com.messaging.security.jwt.JwtService;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.service.UserSessionService;
import com.messaging.user.dto.UserCreateRequest;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserSessionService userSessionService;
  private final AuthRateLimitService rateLimitService;

  public LoginResult register(UserCreateRequest request, SessionRequestMetadata metadata) {
    User user = userService.create(request);
    return createLoginResult(user, metadata);
  }

  public LoginResult login(LoginRequest request, SessionRequestMetadata metadata) {
    rateLimitService.checkLogin(request.identifier(), metadata.ipAddress());
    User user =
        userService
            .findByIdentifier(request.identifier())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    user = userService.getForUpdate(user.getId());

    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72
        || !passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    requireEnabled(user);

    return createLoginResult(user, metadata);
  }

  public LoginResult refresh(String refreshToken, SessionRequestMetadata metadata) {
    if (!jwtService.isValidRefreshToken(refreshToken)) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    User user = userService.getForUpdate(Long.valueOf(jwtService.subject(refreshToken)));
    requireEnabled(user);
    String nextRefreshToken = jwtService.createRefreshToken(user.getId().toString());
    var session =
        userSessionService.rotateRefreshToken(user, refreshToken, nextRefreshToken, metadata);

    String accessToken =
        jwtService.createAccessToken(
            user.getId().toString(), Map.of("sid", session.getAccessKey()));
    return new LoginResult(userService.toLoginResponse(user), accessToken, nextRefreshToken);
  }

  public void logout(String refreshToken) {
    userSessionService.revokeRefreshToken(refreshToken);
  }

  private LoginResult createLoginResult(User user, SessionRequestMetadata metadata) {
    String subject = user.getId().toString();
    String refreshToken = jwtService.createRefreshToken(subject);
    var session = userSessionService.createOrUpdateSession(user, refreshToken, metadata);

    String accessToken =
        jwtService.createAccessToken(subject, Map.of("sid", session.getAccessKey()));
    return new LoginResult(userService.toLoginResponse(user), accessToken, refreshToken);
  }

  private void requireEnabled(User user) {
    if (!AccessPolicy.canSignIn(user)) {
      throw new UnauthorizedException("Account is unavailable");
    }
  }
}
