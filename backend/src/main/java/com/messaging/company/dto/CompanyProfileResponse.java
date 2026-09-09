package com.messaging.company.dto;

public record CompanyProfileResponse(
    String legalName,
    String website,
    String businessEmail,
    String businessPhone,
    String industry,
    String registrationNumber,
    String taxId) {}
