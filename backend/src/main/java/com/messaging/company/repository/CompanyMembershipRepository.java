package com.messaging.company.repository;

import com.messaging.company.entity.CompanyMembership;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {

  @EntityGraph(attributePaths = {"company", "role"})
  @Query(
      """
            select m from CompanyMembership m where m.user.id = :userId
            and m.status = com.messaging.company.entity.MembershipStatus.ACTIVE
            and m.company.status = com.messaging.company.entity.CompanyStatus.ACTIVE and m.role.active = true
            order by m.id
            """)
  Slice<CompanyMembership> findActiveMemberships(Long userId, Pageable pageable);

  @EntityGraph(attributePaths = {"company", "role"})
  Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId, Long companyId);

  @Query(
      value =
          """
            select exists(select 1 from company_memberships m
            join companies c on c.id = m.company_id join roles r on r.id = m.role_id
            where m.user_id = :userId and m.status = 'ACTIVE' and c.status = 'ACTIVE' and r.active = true)
            """,
      nativeQuery = true)
  boolean hasWorkspace(Long userId);
}
