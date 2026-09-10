package com.messaging.subscription.repository;

import com.messaging.company.entity.Company;
import com.messaging.subscription.entity.CompanySubscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Long> {
  @EntityGraph(attributePaths = {"plan", "plan.limits", "plan.pricing"})
  Optional<CompanySubscription> findByCompanyId(Long companyId);

  Optional<CompanySubscription> findByCompany(Company company);
}
