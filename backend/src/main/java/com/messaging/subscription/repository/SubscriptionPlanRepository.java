package com.messaging.subscription.repository;

import com.messaging.subscription.entity.SubscriptionPlan;
import com.messaging.user.entity.AccountType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
  @EntityGraph(attributePaths = {"limits", "pricing"})
  List<SubscriptionPlan> findAllByOrderByMonthlyPriceAsc();

  @EntityGraph(attributePaths = {"limits", "pricing"})
  List<SubscriptionPlan> findByActiveTrueOrderByMonthlyPriceAsc();

  @EntityGraph(attributePaths = {"limits", "pricing"})
  List<SubscriptionPlan> findByAccountTypeAndActiveTrueOrderByMonthlyPriceAsc(
      AccountType accountType);

  @EntityGraph(attributePaths = {"limits", "pricing"})
  Optional<SubscriptionPlan> findByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCase(String code);
}
