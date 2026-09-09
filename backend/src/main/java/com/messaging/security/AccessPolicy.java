package com.messaging.security;

import com.messaging.common.exception.ForbiddenException;
import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.entity.CompanyStatus;
import com.messaging.company.entity.MembershipStatus;
import com.messaging.user.entity.User;
import com.messaging.user.entity.UserStatus;

/** Access rules live here; company roles never grant platform privileges. */
public final class AccessPolicy {
  private AccessPolicy() {}

  public static boolean canSignIn(User user) {
    return user.getStatus() == UserStatus.ACTIVE
        || user.getStatus() == UserStatus.PENDING_VERIFICATION;
  }

  public static void requireActive(User user) {
    if (user.getStatus() != UserStatus.ACTIVE
        || !user.isEmailVerified()
        || user.isPasswordChangeRequired()) {
      throw new ForbiddenException("Verify your email before entering the workspace");
    }
  }

  public static boolean canEnterCompany(CompanyMembership membership) {
    return membership.getStatus() == MembershipStatus.ACTIVE
        && membership.getCompany().getStatus() == CompanyStatus.ACTIVE
        && membership.getRole().isActive();
  }

  public static boolean canManageCompany(CompanyMembership membership) {
    return canEnterCompany(membership)
        && ("OWNER".equals(membership.getRole().getName())
            || "ADMIN".equals(membership.getRole().getName()));
  }
}
