package com.messaging.company.dto;

import com.messaging.company.enums.CompanyRole;
import com.messaging.company.enums.CompanyStatus;

public record CompanyResponse(
    String companyId, String name, String slug, CompanyStatus status, CompanyRole role) {}
