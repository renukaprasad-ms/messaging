package com.messaging.company.repository;

import com.messaging.company.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
}
