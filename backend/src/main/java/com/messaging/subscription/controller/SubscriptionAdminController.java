package com.messaging.subscription.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.subscription.dto.SubscriptionPlanRequest;
import com.messaging.subscription.dto.SubscriptionPlanResponse;
import com.messaging.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionAdminController {

  private final SubscriptionService subscriptionService;

  @GetMapping
  public ApiResponse<List<SubscriptionPlanResponse>> plans() {
    return ApiResponse.success(
        200, subscriptionService.listPlansForAdmin(), "Subscription plans fetched");
  }

  @GetMapping("/{planId}")
  public ApiResponse<SubscriptionPlanResponse> plan(@PathVariable Long planId) {
    return ApiResponse.success(
        200, subscriptionService.getPlanForAdmin(planId), "Subscription plan fetched");
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SubscriptionPlanResponse> create(
      @Valid @RequestBody SubscriptionPlanRequest request) {
    return ApiResponse.success(
        HttpStatus.CREATED.value(),
        subscriptionService.createPlan(request),
        "Subscription plan created");
  }

  @PutMapping("/{planId}")
  public ApiResponse<SubscriptionPlanResponse> update(
      @PathVariable Long planId, @Valid @RequestBody SubscriptionPlanRequest request) {
    return ApiResponse.success(
        200, subscriptionService.updatePlan(planId, request), "Subscription plan updated");
  }

  @DeleteMapping("/{planId}")
  public ApiResponse<Void> delete(@PathVariable Long planId) {
    subscriptionService.deletePlan(planId);
    return ApiResponse.success(200, "Subscription plan deleted");
  }
}
