package com.messaging.user.entity;

import com.messaging.platformrole.entity.PlatformRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_user_email", columnList = "email"),
      @Index(name = "idx_user_status", columnList = "status")
    },
    uniqueConstraints = {@UniqueConstraint(name = "uk_user_email", columnNames = "email")})
public class User {

  @Setter
  @Column(name = "password_change_required", nullable = false)
  private boolean passwordChangeRequired;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_platform_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "platform_role_id"))
  private Set<PlatformRole> platformRoles = new HashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Setter
  @Column(name = "email", nullable = false, length = 320)
  private String email;

  @Setter
  @Column(name = "password", nullable = false)
  private String password;

  @Setter
  @Column(name = "phone", length = 30)
  private String phone;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private UserStatus status = UserStatus.PENDING_VERIFICATION;

  @Setter
  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified = false;

  @Setter
  @Column(name = "phone_verified", nullable = false)
  private boolean phoneVerified = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
