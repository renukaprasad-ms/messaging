package com.messaging.media.service;

import com.messaging.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

  public Long currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("Authentication required");
    }

    Object principal = authentication.getPrincipal();
    if (principal == null) {
      throw new UnauthorizedException("Authentication required");
    }

    try {
      return Long.parseLong(principal.toString());
    } catch (NumberFormatException exception) {
      throw new UnauthorizedException("Invalid authentication principal");
    }
  }
}
