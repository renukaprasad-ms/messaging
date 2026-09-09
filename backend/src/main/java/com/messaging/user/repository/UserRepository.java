package com.messaging.user.repository;

import com.messaging.user.dto.UserSummary;
import com.messaging.user.entity.User;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  @Query(
      "select new com.messaging.user.dto.UserSummary(u.id, u.name, u.email, u.status) from User u order by u.id")
  Slice<UserSummary> listSummaries(Pageable pageable);

  @EntityGraph(attributePaths = "platformRoles")
  @Query(
      """
            select u from User u where u.id = :userId and exists (
                select s.id from UserSession s where s.user = u and s.accessKey = :accessKey
                and s.active = true and s.expiresAt > :now)
            """)
  Optional<User> findAuthenticatedUser(Long userId, String accessKey, Instant now);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.id = :id")
  Optional<User> findForUpdateById(@Param("id") Long id);

  boolean existsByEmail(String email);

  Optional<User> findByEmailOrPhone(String email, String phone);
}
