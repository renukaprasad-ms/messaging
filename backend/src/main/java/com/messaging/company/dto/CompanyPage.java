package com.messaging.company.dto;

import java.util.List;

public record CompanyPage(List<CompanySummaryResponse> companies, boolean hasNext) {}
