package com.messaging.company.repository;

import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.entity.MembershipStatus;
import com.messaging.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {

    List<CompanyMembership> findAllByUserAndStatus(User user, MembershipStatus status);

    boolean existsByUserAndStatus(User user, MembershipStatus status);
}
