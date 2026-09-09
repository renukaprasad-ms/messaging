package com.messaging.company.service;

import com.messaging.common.exception.ForbiddenException;
import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.repository.CompanyMembershipRepository;
import com.messaging.security.AccessPolicy;
import com.messaging.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyAccessService {
  private final CompanyMembershipRepository memberships;
  private final UserService users;

  @Transactional
  public CompanyMembership requireMembership(Long userId, Long companyId, boolean manage) {
    AccessPolicy.requireActive(users.getById(userId));
    CompanyMembership membership =
        memberships
            .findByUserIdAndCompanyId(userId, companyId)
            .orElseThrow(() -> new ForbiddenException("You do not have access to this company"));
    if (!AccessPolicy.canEnterCompany(membership)
        || (manage && !AccessPolicy.canManageCompany(membership))) {
      throw new ForbiddenException("Your company role does not allow this action");
    }
    return membership;
  }
}
