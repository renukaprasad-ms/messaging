package com.messaging.user.controller;

import com.messaging.auth.dto.LoginResponse;
import com.messaging.common.exception.UnauthorizedException;
import com.messaging.common.response.ApiResponse;
import com.messaging.security.AccessPolicy;
import com.messaging.user.dto.UserUpdateRequest;
import com.messaging.user.entity.User;
import com.messaging.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public ApiResponse<LoginResponse> me(@AuthenticationPrincipal String userId) {
    User user = userService.getById(Long.valueOf(userId));
    requireEnabled(user);
    return ApiResponse.success(200, userService.toLoginResponse(user), "Current user");
  }

  @PatchMapping("/me")
  public ApiResponse<LoginResponse> updateMe(
      @AuthenticationPrincipal String userId, @Valid @RequestBody UserUpdateRequest request) {
    User user = userService.updateProfile(Long.valueOf(userId), request);
    requireEnabled(user);
    return ApiResponse.success(200, userService.toLoginResponse(user), "Account updated");
  }

  private void requireEnabled(User user) {
    if (!AccessPolicy.canSignIn(user)) {
      throw new UnauthorizedException("Account is unavailable");
    }
  }
}
