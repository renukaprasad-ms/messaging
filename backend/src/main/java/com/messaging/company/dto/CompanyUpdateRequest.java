package com.messaging.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyUpdateRequest(
    @NotBlank @Size(max = 150) String name, @Size(max = 150) String displayName) {}
