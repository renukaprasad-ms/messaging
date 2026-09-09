package com.messaging.security.jwt;

import com.messaging.security.AccessPolicy;
import com.messaging.security.web.CookieProperties;
import com.messaging.user.entity.User;
import com.messaging.user.entity.UserStatus;
import com.messaging.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CookieProperties cookieProperties;
  private final UserService userService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    accessToken(request)
        .flatMap(jwtService::parseAccessIdentity)
        .flatMap(
            identity ->
                userService.findAuthenticatedUser(
                    identity.userId(), identity.sessionKey(), Instant.now()))
        .filter(AccessPolicy::canSignIn)
        .ifPresent(user -> authenticate(user, request));

    filterChain.doFilter(request, response);
  }

  private Optional<String> accessToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }

    return Arrays.stream(cookies)
        .filter(cookie -> cookieProperties.getAccessName().equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  private void authenticate(User user, HttpServletRequest request) {
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      return;
    }

    var authorities = new ArrayList<SimpleGrantedAuthority>();
    if (user.getStatus() == UserStatus.ACTIVE
        && user.isEmailVerified()
        && !user.isPasswordChangeRequired()) {
      authorities.add(new SimpleGrantedAuthority("WORKSPACE_ACCESS"));
      user.getPlatformRoles().stream()
          .filter(role -> role.isActive())
          .forEach(
              role -> authorities.add(new SimpleGrantedAuthority("PLATFORM_" + role.getName())));
    }
    var authentication =
        new UsernamePasswordAuthenticationToken(user.getId().toString(), null, authorities);
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
