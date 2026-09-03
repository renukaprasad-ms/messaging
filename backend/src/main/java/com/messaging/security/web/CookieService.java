package com.messaging.security.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final CookieProperties properties;

    public String accessTokenCookie(String token) {
        return tokenCookie(properties.getAccessName(), token, properties.getAccessMaxAge());
    }

    public String refreshTokenCookie(String token) {
        return tokenCookie(properties.getRefreshName(), token, properties.getRefreshMaxAge());
    }

    public String clearAccessTokenCookie() {
        return tokenCookie(properties.getAccessName(), "", Duration.ZERO);
    }

    public String clearRefreshTokenCookie() {
        return tokenCookie(properties.getRefreshName(), "", Duration.ZERO);
    }

    public void addAccessTokenCookie(HttpHeaders headers, String token) {
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie(token));
    }

    public void addRefreshTokenCookie(HttpHeaders headers, String token) {
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie(token));
    }

    public void clearTokenCookies(HttpHeaders headers) {
        headers.add(HttpHeaders.SET_COOKIE, clearAccessTokenCookie());
        headers.add(HttpHeaders.SET_COOKIE, clearRefreshTokenCookie());
    }

    private String tokenCookie(String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(properties.isHttpOnly())
                .secure(properties.isSecure())
                .path(properties.getPath())
                .maxAge(maxAge)
                .sameSite(properties.getSameSite());

        if (properties.getDomain() != null && !properties.getDomain().isBlank()) {
            builder.domain(properties.getDomain());
        }

        return builder.build().toString();
    }
}
