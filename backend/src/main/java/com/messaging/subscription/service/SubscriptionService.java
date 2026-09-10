package com.messaging.subscription.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.common.exception.ConflictException;
import com.messaging.common.exception.NotFoundException;
import com.messaging.company.entity.Company;
import com.messaging.company.service.CompanyAccessService;
import com.messaging.subscription.dto.ChannelPricingRequest;
import com.messaging.subscription.dto.ChannelPricingResponse;
import com.messaging.subscription.dto.CompanySubscriptionResponse;
import com.messaging.subscription.dto.PlanLimitRequest;
import com.messaging.subscription.dto.PlanLimitResponse;
import com.messaging.subscription.dto.SubscriptionPlanRequest;
import com.messaging.subscription.dto.SubscriptionPlanResponse;
import com.messaging.subscription.entity.CompanySubscription;
import com.messaging.subscription.entity.SubscriptionChannelPricing;
import com.messaging.subscription.entity.SubscriptionPlan;
import com.messaging.subscription.entity.SubscriptionPlanLimit;
import com.messaging.subscription.entity.SubscriptionStatus;
import com.messaging.subscription.repository.CompanySubscriptionRepository;
import com.messaging.subscription.repository.SubscriptionPlanRepository;
import com.messaging.user.entity.AccountType;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private static final String DEFAULT_ORG_PLAN_CODE = "FREE_ORGANIZATION";

  private final SubscriptionPlanRepository planRepository;
  private final CompanySubscriptionRepository companySubscriptionRepository;
  private final CompanyAccessService companyAccessService;

  @Transactional(readOnly = true)
  @PreAuthorize("hasAnyAuthority('PLATFORM_ADMIN', 'PLATFORM_SUPERADMIN')")
  public List<SubscriptionPlanResponse> listPlansForAdmin() {
    return planRepository.findAllByOrderByMonthlyPriceAsc().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<SubscriptionPlanResponse> listActivePlans() {
    return planRepository.findByActiveTrueOrderByMonthlyPriceAsc().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SubscriptionPlanResponse> listActivePlans(AccountType accountType) {
    if (accountType == null) return listActivePlans();
    return planRepository.findByAccountTypeAndActiveTrueOrderByMonthlyPriceAsc(accountType).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAnyAuthority('PLATFORM_ADMIN', 'PLATFORM_SUPERADMIN')")
  public SubscriptionPlanResponse getPlanForAdmin(Long planId) {
    return toResponse(
        planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found")));
  }

  @Transactional
  @PreAuthorize("hasAnyAuthority('PLATFORM_ADMIN', 'PLATFORM_SUPERADMIN')")
  public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
    String code = normalizeCode(request.code());
    if (planRepository.existsByCodeIgnoreCase(code)) {
      throw new ConflictException("A subscription plan with this code already exists");
    }
    SubscriptionPlan plan = new SubscriptionPlan();
    apply(plan, request, code);
    return toResponse(planRepository.save(plan));
  }

  @Transactional
  @PreAuthorize("hasAnyAuthority('PLATFORM_ADMIN', 'PLATFORM_SUPERADMIN')")
  public SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest request) {
    SubscriptionPlan plan =
        planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found"));
    String code = normalizeCode(request.code());
    planRepository
        .findByCodeIgnoreCase(code)
        .filter(existing -> !existing.getId().equals(planId))
        .ifPresent(
            existing -> {
              throw new ConflictException("A subscription plan with this code already exists");
            });
    apply(plan, request, code);
    return toResponse(plan);
  }

  @Transactional
  @PreAuthorize("hasAnyAuthority('PLATFORM_ADMIN', 'PLATFORM_SUPERADMIN')")
  public void deletePlan(Long planId) {
    SubscriptionPlan plan =
        planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found"));
    plan.setActive(false);
  }

  @Transactional
  public CompanySubscriptionResponse getCompanySubscription(Long companyId, Long userId) {
    Company company = companyAccessService.requireMembership(userId, companyId, false).getCompany();
    return toResponse(assignDefaultPlan(company));
  }

  @Transactional
  public CompanySubscriptionResponse selectPlan(Long companyId, Long userId, String planCode) {
    Company company = companyAccessService.requireMembership(userId, companyId, true).getCompany();
    SubscriptionPlan plan = activePlan(planCode, AccountType.ORGANIZATION);
    CompanySubscription subscription =
        companySubscriptionRepository.findByCompany(company).orElseGet(CompanySubscription::new);
    subscription.setCompany(company);
    subscription.setPlan(plan);
    subscription.setStatus(SubscriptionStatus.ACTIVE);
    subscription.setEndsAt(null);
    subscription.setRenewsAt(null);
    if (subscription.getStartsAt() == null) subscription.setStartsAt(Instant.now());
    return toResponse(companySubscriptionRepository.save(subscription));
  }

  @Transactional
  public CompanySubscription assignDefaultPlan(Company company) {
    return companySubscriptionRepository
        .findByCompany(company)
        .orElseGet(
            () -> {
              CompanySubscription subscription = new CompanySubscription();
              subscription.setCompany(company);
              subscription.setPlan(activePlan(DEFAULT_ORG_PLAN_CODE, AccountType.ORGANIZATION));
              subscription.setStatus(SubscriptionStatus.ACTIVE);
              subscription.setStartsAt(Instant.now());
              return companySubscriptionRepository.save(subscription);
            });
  }

  private void apply(SubscriptionPlan plan, SubscriptionPlanRequest request, String code) {
    validateUniqueLimits(request.limits());
    validateUniquePricing(request.pricing());
    plan.setCode(code);
    plan.setAccountType(request.accountType());
    plan.setName(request.name().trim());
    plan.setMonthlyPrice(request.monthlyPrice());
    plan.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
    plan.setActive(request.active());
    plan.replaceLimits(toLimitEntities(request.limits()));
    plan.replacePricing(toPricingEntities(request.pricing()));
  }

  private SubscriptionPlan activePlan(String planCode, AccountType accountType) {
    SubscriptionPlan plan =
        planRepository
            .findByCodeIgnoreCase(normalizeCode(planCode))
            .orElseThrow(() -> new NotFoundException("Subscription plan not found"));
    if (!plan.isActive()) throw new BadRequestException("Subscription plan is inactive");
    if (plan.getAccountType() != accountType) {
      throw new BadRequestException("Subscription plan is not available for this account type");
    }
    return plan;
  }

  private String normalizeCode(String code) {
    return code.trim().toUpperCase(Locale.ROOT);
  }

  private void validateUniqueLimits(List<PlanLimitRequest> requests) {
    if (requests == null) return;
    Set<String> keys = new HashSet<>();
    for (PlanLimitRequest request : requests) {
      if (!keys.add(request.featureKey().trim())) {
        throw new BadRequestException("Duplicate subscription limit: " + request.featureKey());
      }
    }
  }

  private void validateUniquePricing(List<ChannelPricingRequest> requests) {
    if (requests == null) return;
    Set<String> keys = new HashSet<>();
    for (ChannelPricingRequest request : requests) {
      String key = request.channel() + ":" + request.messageType();
      if (!keys.add(key)) {
        throw new BadRequestException("Duplicate channel pricing: " + key);
      }
    }
  }

  private Set<SubscriptionPlanLimit> toLimitEntities(List<PlanLimitRequest> requests) {
    if (requests == null) return Set.of();
    return requests.stream()
        .map(
            request -> {
              SubscriptionPlanLimit limit = new SubscriptionPlanLimit();
              limit.setFeatureKey(request.featureKey().trim());
              limit.setLimitValue(request.limitValue());
              limit.setUnit(request.unit().trim());
              return limit;
            })
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private Set<SubscriptionChannelPricing> toPricingEntities(List<ChannelPricingRequest> requests) {
    if (requests == null) return Set.of();
    return requests.stream()
        .map(
            request -> {
              SubscriptionChannelPricing pricing = new SubscriptionChannelPricing();
              pricing.setChannel(request.channel());
              pricing.setMessageType(request.messageType());
              pricing.setPricePerSegment(request.pricePerSegment());
              pricing.setFreeDailySegments(request.freeDailySegments());
              pricing.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
              pricing.setActive(request.active());
              return pricing;
            })
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private CompanySubscriptionResponse toResponse(CompanySubscription subscription) {
    return new CompanySubscriptionResponse(
        subscription.getId(),
        subscription.getCompany().getId(),
        subscription.getStatus(),
        subscription.getStartsAt(),
        subscription.getEndsAt(),
        subscription.getRenewsAt(),
        toResponse(subscription.getPlan()));
  }

  private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
    return new SubscriptionPlanResponse(
        plan.getId(),
        plan.getCode(),
        plan.getAccountType(),
        plan.getName(),
        plan.getMonthlyPrice(),
        plan.getCurrency(),
        plan.isActive(),
        plan.getLimits().stream()
            .sorted(Comparator.comparing(SubscriptionPlanLimit::getFeatureKey))
            .map(
                limit ->
                    new PlanLimitResponse(
                        limit.getFeatureKey(), limit.getLimitValue(), limit.getUnit()))
            .toList(),
        plan.getPricing().stream()
            .sorted(
                Comparator.comparing(SubscriptionChannelPricing::getChannel)
                    .thenComparing(SubscriptionChannelPricing::getMessageType))
            .map(
                pricing ->
                    new ChannelPricingResponse(
                        pricing.getChannel(),
                        pricing.getMessageType(),
                        pricing.getPricePerSegment(),
                        pricing.getFreeDailySegments(),
                        pricing.getCurrency(),
                        pricing.isActive()))
            .toList());
  }
}
