package com.messaging.auth.controller;

import com.messaging.auth.dto.RegisterRequest;
import com.messaging.auth.dto.RegisterResponse;
import com.messaging.auth.service.AuthService;
import com.messaging.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
    HttpHeaders headers = new HttpHeaders();
    RegisterResponse response = authService.register(request, servletRequest, headers);

    return ResponseEntity.status(HttpStatus.CREATED)
        .headers(headers)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), response, "Register successful"));
  }
}
