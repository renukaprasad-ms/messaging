package com.messaging.company.dto;

public record CompanyAddressResponse(
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country) {}
