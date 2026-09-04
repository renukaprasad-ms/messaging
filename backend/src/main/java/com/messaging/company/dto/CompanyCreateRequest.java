package com.messaging.company.dto;

public record CompanyCreateRequest(
        String name,
        String displayName,
        String legalName,
        String website,
        String businessEmail,
        String businessPhone,
        String industry,
        String registrationNumber,
        String taxId,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country
) {
}
