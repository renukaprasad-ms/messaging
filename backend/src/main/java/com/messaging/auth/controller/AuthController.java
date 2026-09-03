package com.messaging.auth.controller;

import com.messaging.auth.dto.ForgotPasswordRequest;
import com.messaging.auth.dto.LoginRequest;
import com.messaging.auth.dto.LoginResponse;
import com.messaging.auth.dto.LoginResult;
import com.messaging.auth.dto.ResetPasswordRequest;
import com.messaging.auth.dto.VerifyPasswordResetOtpRequest;
import com.messaging.auth.dto.VerifyPasswordResetOtpResponse;
import com.messaging.auth.service.AuthService;
import com.messaging.auth.service.PasswordResetService;
import com.messaging.common.exception.UnauthorizedException;
import com.messaging.common.response.ApiResponse;
import com.messaging.security.web.CookieService;
import com.messaging.session.service.SessionRequestMetadataResolver;
import jakarta.servlet.http.HttpServletRequest;
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
    private final PasswordResetService passwordResetService;
    private final CookieService cookieService;
    private final SessionRequestMetadataResolver metadataResolver;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @RequestBody UserCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResult loginResult = authService.register(request, metadataResolver.resolve(httpRequest));
        HttpHeaders headers = authCookies(loginResult);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), loginResult.user(), "User created successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResult loginResult = authService.login(request, metadataResolver.resolve(httpRequest));
        HttpHeaders headers = authCookies(loginResult);

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(ApiResponse.success(HttpStatus.OK.value(), loginResult.user(), "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(HttpServletRequest httpRequest) {
        String refreshToken = cookieService.refreshToken(httpRequest)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is missing"));
        LoginResult loginResult = authService.refresh(refreshToken, metadataResolver.resolve(httpRequest));
        HttpHeaders headers = authCookies(loginResult);

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(ApiResponse.success(HttpStatus.OK.value(), loginResult.user(), "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        cookieService.refreshToken(httpRequest).ifPresent(authService::logout);

        HttpHeaders headers = new HttpHeaders();
        cookieService.clearTokenCookies(headers);

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(ApiResponse.success(HttpStatus.OK.value(), "Logout successful"));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ApiResponse.success(HttpStatus.OK.value(), "If the account exists, a reset code has been sent");
    }

    @PostMapping("/verify-reset-otp")
    public ApiResponse<VerifyPasswordResetOtpResponse> verifyResetOtp(
            @RequestBody VerifyPasswordResetOtpRequest request
    ) {
        VerifyPasswordResetOtpResponse response = passwordResetService.verifyOtp(request);
        return ApiResponse.success(HttpStatus.OK.value(), response, "Reset OTP verified successfully");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.success(HttpStatus.OK.value(), "Password reset successfully");
    }

    private HttpHeaders authCookies(LoginResult loginResult) {
        HttpHeaders headers = new HttpHeaders();
        cookieService.addAccessTokenCookie(headers, loginResult.accessToken());
        cookieService.addRefreshTokenCookie(headers, loginResult.refreshToken());
        return headers;
    }
}
