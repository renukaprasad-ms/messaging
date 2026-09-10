package com.messaging.company.dto;

public record CompanyResponse(
    Long id,
    String name,
    String displayName,
    String logoUrl,
    String status,
    String role,
    CompanyProfileResponse profile,
    CompanyAddressResponse address) {}
