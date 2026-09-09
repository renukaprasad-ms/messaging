package com.messaging.platformrole.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.platformrole.service.PlatformAdminService;
import com.messaging.user.dto.UserPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class PlatformAdminController {
  private final PlatformAdminService platformAdminService;

  @GetMapping("/users")
  public ApiResponse<UserPage> users(@RequestParam(defaultValue = "0") int page) {
    return ApiResponse.success(200, platformAdminService.listUsers(page), "Users fetched");
  }
}
