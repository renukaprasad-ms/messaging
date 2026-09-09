package com.messaging.company.dto;

import java.util.List;

public record CompanyPage(List<CompanyResponse> companies, boolean hasNext) {}
