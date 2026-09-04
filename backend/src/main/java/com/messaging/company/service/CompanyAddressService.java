package com.messaging.company.service;

import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.entity.Company;
import com.messaging.company.entity.CompanyAddress;
import com.messaging.company.repository.CompanyAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyAddressService {

    private final CompanyAddressRepository companyAddressRepository;

    public CompanyAddress createAddress(Company company, CompanyCreateRequest request) {
        CompanyAddress address = new CompanyAddress();
        address.setCompany(company);
        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        return companyAddressRepository.save(address);
    }
}
