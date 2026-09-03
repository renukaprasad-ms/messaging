package com.messaging.auth.controller;

import com.messaging.auth.dto.LoginRequest;
import com.messaging.auth.dto.LoginResponse;
import com.messaging.auth.dto.LoginResult;
import com.messaging.auth.service.AuthService;
import com.messaging.common.response.ApiResponse;
import com.messaging.security.web.CookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.messaging.user.dto.UserCreateRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@RequestBody UserCreateRequest request) {
        LoginResult loginResult = authService.register(request);
        HttpHeaders headers = authCookies(loginResult);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), loginResult.user(), "User created successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResult loginResult = authService.login(request);
        HttpHeaders headers = authCookies(loginResult);

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(ApiResponse.success(HttpStatus.OK.value(), loginResult.user(), "Login successful"));
    }

    private HttpHeaders authCookies(LoginResult loginResult) {
        HttpHeaders headers = new HttpHeaders();
        cookieService.addAccessTokenCookie(headers, loginResult.accessToken());
        cookieService.addRefreshTokenCookie(headers, loginResult.refreshToken());
        return headers;
    }
}
