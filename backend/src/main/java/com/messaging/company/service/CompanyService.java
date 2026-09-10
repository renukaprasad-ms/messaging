package com.messaging.company.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.company.dto.CompanyAddressResponse;
import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.dto.CompanyPage;
import com.messaging.company.dto.CompanyProfileResponse;
import com.messaging.company.dto.CompanyResponse;
import com.messaging.company.dto.CompanySummaryResponse;
import com.messaging.company.dto.CompanyUpdateRequest;
import com.messaging.company.entity.Company;
import com.messaging.company.entity.CompanyAddress;
import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.entity.CompanyProfile;
import com.messaging.company.repository.CompanyRepository;
import com.messaging.role.entity.Role;
import com.messaging.role.service.RoleService;
import com.messaging.security.AccessPolicy;
import com.messaging.subscription.service.SubscriptionService;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final CompanyProfileService companyProfileService;
  private final CompanyAddressService companyAddressService;
  private final CompanyVerificationService companyVerificationService;
  private final CompanyMembershipService companyMembershipService;
  private final RoleService roleService;
  private final UserService userService;
  private final CompanyAccessService companyAccessService;
  private final SubscriptionService subscriptionService;

  @Transactional
  public CompanyResponse getCompany(Long companyId, Long userId) {
    User user = userService.getById(userId);
    AccessPolicy.requireUsableAccount(user);
    if (AccessPolicy.isPlatformAdmin(user)) {
      Company company =
          companyRepository
              .findById(companyId)
              .orElseThrow(() -> new BadRequestException("Company not found"));
      return toResponse(company, AccessPolicy.platformCompanyRole(user));
    }
    return toResponse(companyAccessService.requireMembership(userId, companyId, false));
  }

  @Transactional
  public CompanyResponse updateCompany(Long companyId, Long userId, CompanyUpdateRequest request) {
    CompanyMembership membership = companyAccessService.requireMembership(userId, companyId, true);
    membership.getCompany().setName(request.name().trim());
    membership.getCompany().setDisplayName(request.displayName());
    return toResponse(membership);
  }

  @Transactional
  public CompanyResponse createCompany(CompanyCreateRequest request, Long userId) {
    User user = userService.getById(userId);
    AccessPolicy.requireOrganizationAccount(user);
    Role ownerRole = roleService.getByName("OWNER");

    Company company = new Company();
    company.setName(request.name());
    company.setDisplayName(request.displayName());
    company.setLogoUrl(request.logoUrl());
    Company savedCompany = companyRepository.save(company);

    companyProfileService.createProfile(savedCompany, request);
    companyAddressService.createAddress(savedCompany, request);
    companyVerificationService.createPendingVerification(savedCompany);

    CompanyMembership membership =
        companyMembershipService.createOwnerMembership(savedCompany, user, ownerRole);
    subscriptionService.assignDefaultPlan(savedCompany);
    return toResponse(membership);
  }

  @Transactional(readOnly = true)
  public CompanyPage getCompaniesForUser(Long userId, int page) {
    if (page < 0) throw new BadRequestException("Page must not be negative");
    User user = userService.getById(userId);
    AccessPolicy.requireUsableAccount(user);
    if (AccessPolicy.isPlatformAdmin(user)) {
      var companies = companyRepository.findAllByOrderById(PageRequest.of(page, 25));
      String role = AccessPolicy.platformCompanyRole(user);
      return new CompanyPage(
          companies.stream().map(company -> toSummary(company, role)).toList(),
          companies.hasNext());
    }
    var memberships = companyMembershipService.getActiveMemberships(userId, page);
    return new CompanyPage(
        memberships.stream().map(this::toSummary).toList(), memberships.hasNext());
  }

  private CompanyResponse toResponse(CompanyMembership membership) {
    Company company = membership.getCompany();
    return toResponse(company, membership.getRole().getName());
  }

  private CompanySummaryResponse toSummary(CompanyMembership membership) {
    Company company = membership.getCompany();
    return toSummary(company, membership.getRole().getName());
  }

  private CompanySummaryResponse toSummary(Company company, String role) {
    return new CompanySummaryResponse(
        company.getId(),
        company.getName(),
        company.getDisplayName(),
        company.getLogoUrl(),
        company.getStatus().name(),
        role);
  }

  private CompanyResponse toResponse(Company company, String role) {
    CompanyProfile profile = companyProfileService.getByCompanyId(company.getId());
    CompanyAddress address = companyAddressService.getPrimaryAddress(company.getId());
    return new CompanyResponse(
        company.getId(),
        company.getName(),
        company.getDisplayName(),
        company.getLogoUrl(),
        company.getStatus().name(),
        role,
        profile == null
            ? null
            : new CompanyProfileResponse(
                profile.getLegalName(),
                profile.getWebsite(),
                profile.getBusinessEmail(),
                profile.getBusinessPhone(),
                profile.getIndustry()),
        address == null
            ? null
            : new CompanyAddressResponse(
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()));
  }
}
