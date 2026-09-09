package com.messaging.company.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.dto.CompanyPage;
import com.messaging.company.dto.CompanyResponse;
import com.messaging.company.dto.CompanyUpdateRequest;
import com.messaging.company.entity.Company;
import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.repository.CompanyRepository;
import com.messaging.role.entity.Role;
import com.messaging.role.service.RoleService;
import com.messaging.security.AccessPolicy;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import lombok.RequiredArgsConstructor;
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

  @Transactional
  public CompanyResponse getCompany(Long companyId, Long userId) {
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
    AccessPolicy.requireActive(user);
    Role ownerRole = roleService.getByName("OWNER");

    Company company = new Company();
    company.setName(request.name());
    company.setDisplayName(request.displayName());
    Company savedCompany = companyRepository.save(company);

    companyProfileService.createProfile(savedCompany, request);
    companyAddressService.createAddress(savedCompany, request);
    companyVerificationService.createPendingVerification(savedCompany);

    CompanyMembership membership =
        companyMembershipService.createOwnerMembership(savedCompany, user, ownerRole);
    return toResponse(membership);
  }

  @Transactional(readOnly = true)
  public CompanyPage getCompaniesForUser(Long userId, int page) {
    if (page < 0) throw new BadRequestException("Page must not be negative");
    User user = userService.getById(userId);
    AccessPolicy.requireActive(user);
    var memberships = companyMembershipService.getActiveMemberships(userId, page);
    return new CompanyPage(
        memberships.stream().map(this::toResponse).toList(), memberships.hasNext());
  }

  private CompanyResponse toResponse(CompanyMembership membership) {
    Company company = membership.getCompany();
    return new CompanyResponse(
        company.getId(),
        company.getName(),
        company.getDisplayName(),
        company.getStatus().name(),
        membership.getRole().getName());
  }
}
