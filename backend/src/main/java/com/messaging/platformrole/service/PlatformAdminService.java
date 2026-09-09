package com.messaging.platformrole.service;

import com.messaging.user.dto.UserPage;
import com.messaging.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformAdminService {
  private final UserService userService;

  @PreAuthorize("hasAnyAuthority('PLATFORM_ADMIN', 'PLATFORM_SUPERADMIN')")
  public UserPage listUsers(int page) {
    return userService.listUsers(page);
  }
}
