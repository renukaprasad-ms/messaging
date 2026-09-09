package com.messaging.company.repository;

import com.messaging.company.entity.CompanyAddress;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyAddressRepository extends JpaRepository<CompanyAddress, Long> {
  Optional<CompanyAddress> findFirstByCompanyIdOrderById(Long companyId);
}
