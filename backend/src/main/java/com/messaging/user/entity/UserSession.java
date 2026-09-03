package com.messaging.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "user_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_platform",
                        columnNames = {"user_id", "platform"}
                )
        },
        indexes = {
                @Index(name = "idx_session_user_id", columnList = "user_id"),
                @Index(name = "idx_session_refresh_token", columnList = "refresh_token")
        }
)
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private SessionPlatform platform;

    @Setter
    @Column(name = "refresh_token", nullable = false, length = 500)
    private String refreshToken;

    @Setter
    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Setter
    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Setter
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Setter
    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Setter
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Setter
    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Setter
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;

        if (lastActiveAt == null) {
            lastActiveAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
