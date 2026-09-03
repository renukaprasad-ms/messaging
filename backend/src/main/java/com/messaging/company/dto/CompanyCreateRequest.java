package com.messaging.company.dto;

public record CompanyCreateRequest(
        String name,
        String displayName
) {
}
