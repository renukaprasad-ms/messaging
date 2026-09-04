package com.messaging.company.repository;

import com.messaging.company.entity.CompanyVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, Long> {
}
