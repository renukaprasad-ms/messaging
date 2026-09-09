package com.messaging.app;

import static org.junit.jupiter.api.Assertions.*;

import com.messaging.company.entity.*;
import com.messaging.role.entity.Role;
import com.messaging.security.AccessPolicy;
import com.messaging.user.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AccessPolicyTests {
  @ParameterizedTest
  @ValueSource(strings = {"OWNER", "ADMIN", "MANAGER", "MEMBER"})
  void companyRoleMatrix(String roleName) {
    var membership = new CompanyMembership();
    var role = new Role();
    role.setName(roleName);
    membership.setRole(role);
    membership.setCompany(new Company());
    assertTrue(AccessPolicy.canEnterCompany(membership));
    assertEquals(
        roleName.equals("OWNER") || roleName.equals("ADMIN"),
        AccessPolicy.canManageCompany(membership));
    membership.setStatus(MembershipStatus.SUSPENDED);
    assertFalse(AccessPolicy.canManageCompany(membership));
    assertFalse(AccessPolicy.canEnterCompany(membership));
  }

  @Test
  void blockedAccountsCannotSignInAndPendingAccountsCannotEnterWorkspace() {
    var user = new User();
    assertTrue(AccessPolicy.canSignIn(user));
    assertThrows(RuntimeException.class, () -> AccessPolicy.requireActive(user));
    user.setStatus(UserStatus.SUSPENDED);
    assertFalse(AccessPolicy.canSignIn(user));
    user.setStatus(UserStatus.DISABLED);
    assertFalse(AccessPolicy.canSignIn(user));
  }
}
