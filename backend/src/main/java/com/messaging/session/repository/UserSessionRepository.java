package com.messaging.session.repository;

import com.messaging.session.entity.SessionPlatform;
import com.messaging.session.entity.UserSession;
import com.messaging.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByUserAndPlatform(User user, SessionPlatform platform);

    Optional<UserSession> findByUserAndRefreshTokenAndActiveTrue(User user, String refreshToken);

    Optional<UserSession> findByRefreshTokenAndActiveTrue(String refreshToken);
}
