package com.messaging.auth.service;

import com.messaging.auth.dto.LoginRequest;
import com.messaging.auth.dto.LoginResponse;
import com.messaging.auth.dto.LoginResult;
import com.messaging.company.service.CompanyMembershipService;
import com.messaging.common.exception.UnauthorizedException;
import com.messaging.security.jwt.JwtService;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.service.UserSessionService;
import com.messaging.user.dto.UserCreateRequest;
import com.messaging.user.entity.User;
import com.messaging.user.repository.UserRepository;
import com.messaging.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserSessionService userSessionService;
    private final CompanyMembershipService companyMembershipService;

    public LoginResult register(UserCreateRequest request, SessionRequestMetadata metadata) {
        User user = userService.create(request);
        return createLoginResult(user, metadata);
    }

    public LoginResult login(LoginRequest request, SessionRequestMetadata metadata) {
        User user = userRepository.findByEmailOrPhone(request.identifier(), request.identifier())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return createLoginResult(user, metadata);
    }

    public LoginResult refresh(String refreshToken, SessionRequestMetadata metadata) {
        if (!jwtService.isValidRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        User user = userService.getById(Long.valueOf(jwtService.subject(refreshToken)));
        String nextRefreshToken = jwtService.createRefreshToken(user.getId().toString());
        userSessionService.rotateRefreshToken(user, refreshToken, nextRefreshToken, metadata);

        String accessToken = jwtService.createAccessToken(user.getId().toString());
        return new LoginResult(
                createLoginResponse(user),
                accessToken,
                nextRefreshToken);
    }

    public void logout(String refreshToken) {
        userSessionService.revokeRefreshToken(refreshToken);
    }

    private LoginResult createLoginResult(User user, SessionRequestMetadata metadata) {
        String subject = user.getId().toString();
        String refreshToken = jwtService.createRefreshToken(subject);
        userSessionService.createOrUpdateSession(user, refreshToken, metadata);

        String accessToken = jwtService.createAccessToken(subject);
        return new LoginResult(
                createLoginResponse(user),
                accessToken,
                refreshToken);
    }

    private LoginResponse createLoginResponse(User user) {
        boolean hasCompany = companyMembershipService.hasActiveMembership(user);
        return new LoginResponse(
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                hasCompany,
                user.getStatus());
    }
}
