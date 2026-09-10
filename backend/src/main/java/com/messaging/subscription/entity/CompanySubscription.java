package com.messaging.subscription.entity;

import com.messaging.company.entity.Company;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(
    name = "company_subscriptions",
    indexes = {@Index(name = "idx_company_subscription_status", columnList = "status")},
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_company_subscription_company", columnNames = "company_id")
    })
public class CompanySubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private SubscriptionPlan plan;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

  @Setter
  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Setter
  @Column(name = "ends_at")
  private Instant endsAt;

  @Setter
  @Column(name = "renews_at")
  private Instant renewsAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (startsAt == null) startsAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
