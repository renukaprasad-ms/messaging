package com.messaging.company.repository;

import com.messaging.company.entity.CompanyProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
  Optional<CompanyProfile> findByCompanyId(Long companyId);
}
