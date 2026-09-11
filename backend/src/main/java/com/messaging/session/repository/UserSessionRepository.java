package com.messaging.session.repository;

import com.messaging.session.entity.SessionPlatform;
import com.messaging.session.entity.UserSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

  Optional<UserSession> findByUserIdAndPlatform(UUID userId, SessionPlatform platform);
}
