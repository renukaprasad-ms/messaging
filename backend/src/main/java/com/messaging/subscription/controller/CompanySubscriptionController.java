package com.messaging.subscription.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.subscription.dto.CompanySubscriptionResponse;
import com.messaging.subscription.dto.SelectSubscriptionPlanRequest;
import com.messaging.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/{companyId}/subscription")
@RequiredArgsConstructor
public class CompanySubscriptionController {

  private final SubscriptionService subscriptionService;

  @GetMapping
  public ApiResponse<CompanySubscriptionResponse> subscription(
      @PathVariable Long companyId, @AuthenticationPrincipal String userId) {
    return ApiResponse.success(
        200,
        subscriptionService.getCompanySubscription(companyId, Long.valueOf(userId)),
        "Company subscription fetched");
  }

  @PostMapping("/select")
  public ApiResponse<CompanySubscriptionResponse> selectPlan(
      @PathVariable Long companyId,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody SelectSubscriptionPlanRequest request) {
    return ApiResponse.success(
        200,
        subscriptionService.selectPlan(companyId, Long.valueOf(userId), request.planCode()),
        "Subscription plan selected");
  }
}
