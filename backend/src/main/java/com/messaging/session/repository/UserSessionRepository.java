package com.messaging.session.repository;

import com.messaging.session.entity.SessionPlatform;
import com.messaging.session.entity.UserSession;
import com.messaging.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

  @Modifying(flushAutomatically = true)
  @Query("update UserSession s set s.active = false where s.id = :id")
  void revokeById(Long id);

  @Modifying(flushAutomatically = true)
  @Query("update UserSession s set s.active = false where s.user.id = :userId and s.active = true")
  void revokeByUserId(Long userId);

  boolean existsByUserIdAndAccessKeyAndActiveTrueAndExpiresAtAfter(
      Long userId, String accessKey, Instant now);

  Optional<UserSession> findByUserAndPlatform(User user, SessionPlatform platform);

  Optional<UserSession> findByUserAndRefreshTokenAndActiveTrue(User user, String refreshToken);

  Optional<UserSession> findByRefreshTokenAndActiveTrue(String refreshToken);

  List<UserSession> findAllByUserAndActiveTrue(User user);
}
