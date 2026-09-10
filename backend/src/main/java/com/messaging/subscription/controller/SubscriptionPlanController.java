package com.messaging.subscription.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.subscription.dto.SubscriptionPlanResponse;
import com.messaging.subscription.service.SubscriptionService;
import com.messaging.user.entity.AccountType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

  private final SubscriptionService subscriptionService;

  @GetMapping
  public ApiResponse<List<SubscriptionPlanResponse>> plans(
      @RequestParam(required = false) AccountType accountType) {
    return ApiResponse.success(
        200, subscriptionService.listActivePlans(accountType), "Subscription plans fetched");
  }
}
