package com.messaging.subscription.entity;

import com.messaging.user.entity.AccountType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(
    name = "subscription_plans",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_subscription_plan_code", columnNames = "code")
    })
public class SubscriptionPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Setter
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false, length = 30)
  private AccountType accountType;

  @Setter
  @Column(name = "monthly_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal monthlyPrice;

  @Setter
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Setter
  @Column(name = "active", nullable = false)
  private boolean active = true;

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SubscriptionPlanLimit> limits = new LinkedHashSet<>();

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SubscriptionChannelPricing> pricing = new LinkedHashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public void replaceLimits(Set<SubscriptionPlanLimit> nextLimits) {
    limits.clear();
    nextLimits.forEach(
        limit -> {
          limit.setPlan(this);
          limits.add(limit);
        });
  }

  public void replacePricing(Set<SubscriptionChannelPricing> nextPricing) {
    pricing.clear();
    nextPricing.forEach(
        item -> {
          item.setPlan(this);
          pricing.add(item);
        });
  }

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
