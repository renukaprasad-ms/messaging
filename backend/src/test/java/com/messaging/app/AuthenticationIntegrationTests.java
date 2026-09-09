package com.messaging.app;

import static org.junit.jupiter.api.Assertions.*;

import com.messaging.auth.dto.ChangePasswordRequest;
import com.messaging.auth.dto.LoginRequest;
import com.messaging.auth.dto.ResetPasswordRequest;
import com.messaging.auth.service.AuthService;
import com.messaging.auth.service.PasswordResetService;
import com.messaging.common.exception.UnauthorizedException;
import com.messaging.common.util.HashUtils;
import com.messaging.security.jwt.JwtService;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.entity.SessionPlatform;
import com.messaging.user.dto.UserCreateRequest;
import com.messaging.user.entity.UserStatus;
import com.messaging.user.repository.UserRepository;
import com.messaging.user.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthenticationIntegrationTests extends IntegrationTestSupport {
  @Autowired AuthService auth;
  @Autowired UserService users;
  @Autowired UserRepository userRepository;
  @Autowired JwtService jwt;
  @Autowired PasswordResetService passwords;
  @Autowired PasswordEncoder encoder;
  @Autowired StringRedisTemplate redis;

  private static final String PASSWORD = "Initial-password-1!";
  private static final SessionRequestMetadata META =
      new SessionRequestMetadata(SessionPlatform.WEB, "test", "test", "127.0.0.1", "test");

  private String account() {
    String email = UUID.randomUUID() + "@example.com";
    auth.register(new UserCreateRequest("Test User", email, PASSWORD, PASSWORD, null), META);
    return email;
  }

  @Test
  void concurrentRefreshAllowsOnlyOneWinner() throws Exception {
    String email = account();
    var login = auth.login(new LoginRequest(email, PASSWORD, false), META);
    var start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      Callable<Boolean> refresh =
          () -> {
            start.await();
            try {
              auth.refresh(login.refreshToken(), META);
              return true;
            } catch (UnauthorizedException expected) {
              return false;
            }
          };
      var first = executor.submit(refresh);
      var second = executor.submit(refresh);
      start.countDown();
      assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  void logoutInvalidatesAccessAndRefreshTokens() {
    var login = auth.login(new LoginRequest(account(), PASSWORD, false), META);
    Long id = Long.valueOf(jwt.subject(login.accessToken()));
    assertTrue(
        users
            .findAuthenticatedUser(id, jwt.sessionKey(login.accessToken()), Instant.now())
            .isPresent());
    auth.logout(login.refreshToken());
    assertTrue(
        users
            .findAuthenticatedUser(id, jwt.sessionKey(login.accessToken()), Instant.now())
            .isEmpty());
    assertThrows(UnauthorizedException.class, () -> auth.refresh(login.refreshToken(), META));
  }

  @Test
  void suspendedUsersCannotLoginOrRefresh() {
    String email = account();
    var login = auth.login(new LoginRequest(email, PASSWORD, false), META);
    var user = userRepository.findByEmailOrPhone(email, email).orElseThrow();
    user.setStatus(UserStatus.SUSPENDED);
    userRepository.save(user);
    assertThrows(
        UnauthorizedException.class,
        () -> auth.login(new LoginRequest(email, PASSWORD, false), META));
    assertThrows(UnauthorizedException.class, () -> auth.refresh(login.refreshToken(), META));
  }

  @Test
  void passwordResetTokenIsSingleUseAndRevokesExistingSessions() {
    var login = auth.login(new LoginRequest(account(), PASSWORD, false), META);
    String id = jwt.subject(login.accessToken());
    String token = UUID.randomUUID().toString();
    redis
        .opsForValue()
        .set("password-reset:token:" + HashUtils.sha256Hex(token), id, Duration.ofMinutes(1));
    var request = new ResetPasswordRequest(token, "New-password-2!", "New-password-2!");
    passwords.resetPassword(request);
    assertTrue(encoder.matches("New-password-2!", users.getById(Long.valueOf(id)).getPassword()));
    assertThrows(RuntimeException.class, () -> passwords.resetPassword(request));
    assertTrue(
        users
            .findAuthenticatedUser(
                Long.valueOf(id), jwt.sessionKey(login.accessToken()), Instant.now())
            .isEmpty());
  }

  @Test
  void passwordChangeRevokesSessions() {
    String email = account();
    var login = auth.login(new LoginRequest(email, PASSWORD, false), META);
    Long id = Long.valueOf(jwt.subject(login.accessToken()));
    passwords.changePassword(
        id, new ChangePasswordRequest(PASSWORD, "New-password-2!", "New-password-2!"));
    assertThrows(UnauthorizedException.class, () -> auth.refresh(login.refreshToken(), META));
    assertFalse(
        auth.login(new LoginRequest(email, "New-password-2!", false), META)
            .user()
            .passwordChangeRequired());
  }

  @Test
  void bootstrapAccountsHaveOnlyHashedPasswordsAndSuperadminRole() {
    for (String email : new String[] {"renukaprasadms00@gmail.com", "renukaprasad.dev@gmail.com"}) {
      var user = userRepository.findByEmailOrPhone(email, email).orElseThrow();
      assertTrue(user.getPassword().startsWith("$2"));
      assertTrue(user.isPasswordChangeRequired());
      assertTrue(
          user.getPlatformRoles().stream().anyMatch(role -> role.getName().equals("SUPERADMIN")));
    }
  }
}
