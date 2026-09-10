package com.messaging.subscription.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(
    name = "subscription_channel_pricing",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_subscription_channel_pricing",
          columnNames = {"plan_id", "channel", "message_type"})
    })
public class SubscriptionChannelPricing {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private SubscriptionPlan plan;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(name = "channel", nullable = false, length = 30)
  private BillingChannel channel;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", nullable = false, length = 30)
  private MessageType messageType;

  @Setter
  @Column(name = "price_per_segment", nullable = false, precision = 12, scale = 4)
  private BigDecimal pricePerSegment;

  @Setter
  @Column(name = "free_daily_segments", nullable = false)
  private int freeDailySegments;

  @Setter
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Setter
  @Column(name = "active", nullable = false)
  private boolean active = true;

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
