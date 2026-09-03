package com.messaging.company.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.dto.CompanyResponse;
import com.messaging.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ApiResponse<CompanyResponse> createCompany(
            @AuthenticationPrincipal String userId,
            @RequestBody CompanyCreateRequest request
    ) {
        CompanyResponse response = companyService.createCompany(request, Long.valueOf(userId));
        return ApiResponse.success(HttpStatus.CREATED.value(), response, "Company created successfully");
    }

    @GetMapping
    public ApiResponse<List<CompanyResponse>> getCompanies(@AuthenticationPrincipal String userId) {
        List<CompanyResponse> response = companyService.getCompaniesForUser(Long.valueOf(userId));
        return ApiResponse.success(HttpStatus.OK.value(), response, "Companies fetched successfully");
    }
}
