package com.messaging.company.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.dto.CompanyResponse;
import com.messaging.company.entity.Company;
import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.repository.CompanyRepository;
import com.messaging.role.entity.Role;
import com.messaging.role.service.RoleService;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMembershipService companyMembershipService;
    private final RoleService roleService;
    private final UserService userService;

    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest request, Long userId) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Company name is required");
        }

        User user = userService.getById(userId);
        Role ownerRole = roleService.getByName("OWNER");

        Company company = new Company();
        company.setName(request.name());
        company.setDisplayName(request.displayName());
        Company savedCompany = companyRepository.save(company);

        CompanyMembership membership = companyMembershipService.createOwnerMembership(savedCompany, user, ownerRole);
        return toResponse(membership);
    }

    public List<CompanyResponse> getCompaniesForUser(Long userId) {
        User user = userService.getById(userId);
        return companyMembershipService.getActiveMemberships(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CompanyResponse toResponse(CompanyMembership membership) {
        Company company = membership.getCompany();
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDisplayName(),
                company.getStatus().name(),
                membership.getRole().getName()
        );
    }
}
