package com.messaging.company.dto;

public record CompanySummaryResponse(
    Long id, String name, String displayName, String status, String role) {}
