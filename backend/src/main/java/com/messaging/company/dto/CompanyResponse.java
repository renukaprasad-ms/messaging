package com.messaging.company.dto;

public record CompanyResponse(
        Long id,
        String name,
        String displayName,
        String status,
        String role
) {
}
