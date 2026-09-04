package com.messaging.company.service;

import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.entity.Company;
import com.messaging.company.entity.CompanyProfile;
import com.messaging.company.repository.CompanyProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

    private final CompanyProfileRepository companyProfileRepository;

    public CompanyProfile createProfile(Company company, CompanyCreateRequest request) {
        CompanyProfile profile = new CompanyProfile();
        profile.setCompany(company);
        profile.setLegalName(request.legalName());
        profile.setWebsite(request.website());
        profile.setBusinessEmail(request.businessEmail());
        profile.setBusinessPhone(request.businessPhone());
        profile.setIndustry(request.industry());
        profile.setRegistrationNumber(request.registrationNumber());
        profile.setTaxId(request.taxId());
        return companyProfileRepository.save(profile);
    }
}
