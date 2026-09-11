package com.messaging.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
      @UniqueConstraint(name = "uk_users_username", columnNames = "username")
    })
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(name = "is_verified", nullable = false)
  private boolean verified;

  @Column(name = "profile_picture")
  private String profilePicture;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(name = "two_factor_enabled", nullable = false)
  private boolean twoFactorEnabled;
}
