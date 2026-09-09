package com.messaging.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyCreateRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 150) String displayName,
    @NotBlank @Size(max = 200) String legalName,
    @Size(max = 255) String website,
    @Size(max = 150) String businessEmail,
    @Size(max = 50) String businessPhone,
    @Size(max = 100) String industry,
    @Size(max = 100) String registrationNumber,
    @Size(max = 100) String taxId,
    @NotBlank @Size(max = 255) String addressLine1,
    @Size(max = 255) String addressLine2,
    @NotBlank @Size(max = 100) String city,
    @Size(max = 100) String state,
    @Size(max = 30) String postalCode,
    @NotBlank @Size(max = 100) String country) {}
