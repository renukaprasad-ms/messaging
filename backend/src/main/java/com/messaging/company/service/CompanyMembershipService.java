package com.messaging.company.service;

import com.messaging.company.entity.Company;
import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.entity.MembershipStatus;
import com.messaging.company.repository.CompanyMembershipRepository;
import com.messaging.role.entity.Role;
import com.messaging.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyMembershipService {

    private final CompanyMembershipRepository companyMembershipRepository;

    public CompanyMembership createOwnerMembership(Company company, User user, Role ownerRole) {
        CompanyMembership membership = new CompanyMembership();
        membership.setCompany(company);
        membership.setUser(user);
        membership.setRole(ownerRole);
        membership.setStatus(MembershipStatus.ACTIVE);
        return companyMembershipRepository.save(membership);
    }

    public List<CompanyMembership> getActiveMemberships(User user) {
        return companyMembershipRepository.findAllByUserAndStatus(user, MembershipStatus.ACTIVE);
    }
}
