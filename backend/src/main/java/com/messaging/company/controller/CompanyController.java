package com.messaging.company.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.dto.CompanyPage;
import com.messaging.company.dto.CompanyResponse;
import com.messaging.company.dto.CompanyUpdateRequest;
import com.messaging.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

  private final CompanyService companyService;

  @GetMapping("/{companyId}")
  public ApiResponse<CompanyResponse> getCompany(
      @PathVariable Long companyId, @AuthenticationPrincipal String userId) {
    return ApiResponse.success(
        200, companyService.getCompany(companyId, Long.valueOf(userId)), "Company fetched");
  }

  @PatchMapping("/{companyId}")
  public ApiResponse<CompanyResponse> updateCompany(
      @PathVariable Long companyId,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody CompanyUpdateRequest request) {
    return ApiResponse.success(
        200,
        companyService.updateCompany(companyId, Long.valueOf(userId), request),
        "Company updated");
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CompanyResponse> createCompany(
      @AuthenticationPrincipal String userId, @Valid @RequestBody CompanyCreateRequest request) {
    CompanyResponse response = companyService.createCompany(request, Long.valueOf(userId));
    return ApiResponse.success(
        HttpStatus.CREATED.value(), response, "Company created successfully");
  }

  @GetMapping
  public ApiResponse<CompanyPage> getCompanies(
      @AuthenticationPrincipal String userId, @RequestParam(defaultValue = "0") int page) {
    CompanyPage response = companyService.getCompaniesForUser(Long.valueOf(userId), page);
    return ApiResponse.success(HttpStatus.OK.value(), response, "Companies fetched successfully");
  }
}
