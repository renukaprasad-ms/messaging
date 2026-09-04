package com.messaging.company.service;

import com.messaging.company.entity.Company;
import com.messaging.company.entity.CompanyVerification;
import com.messaging.company.repository.CompanyVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyVerificationService {

    private final CompanyVerificationRepository companyVerificationRepository;

    public CompanyVerification createPendingVerification(Company company) {
        CompanyVerification verification = new CompanyVerification();
        verification.setCompany(company);
        return companyVerificationRepository.save(verification);
    }
}
