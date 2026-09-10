package com.messaging.subscription.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(
    name = "subscription_plan_limits",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_subscription_plan_limit",
          columnNames = {"plan_id", "feature_key"})
    })
public class SubscriptionPlanLimit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private SubscriptionPlan plan;

  @Setter
  @Column(name = "feature_key", nullable = false, length = 100)
  private String featureKey;

  @Setter
  @Column(name = "limit_value", nullable = false, precision = 12, scale = 2)
  private BigDecimal limitValue;

  @Setter
  @Column(name = "unit", nullable = false, length = 50)
  private String unit;

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
