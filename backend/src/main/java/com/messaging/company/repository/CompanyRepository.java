package com.messaging.company.repository;

import com.messaging.company.entity.Company;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
  Slice<Company> findAllByOrderById(Pageable pageable);
}
