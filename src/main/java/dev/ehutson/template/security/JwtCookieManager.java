package dev.ehutson.template.security;

import dev.ehutson.template.security.config.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtCookieManager {
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final JwtProperties properties;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .maxAge(properties.getAccessTokenExpirationSeconds())
                .httpOnly(properties.isHttpOnly())
                .secure(properties.isSecure())
                .path(properties.getPath())
                .sameSite(properties.getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .maxAge(properties.getRefreshTokenExpirationSeconds())
                .httpOnly(properties.isHttpOnly())
                .secure(properties.isSecure())
                .path(properties.getPath())
                .sameSite(properties.getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .maxAge(0)
                .httpOnly(properties.isHttpOnly())
                .secure(properties.isSecure())
                .path(properties.getPath())
                .sameSite(properties.getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .maxAge(0)
                .httpOnly(properties.isHttpOnly())
                .secure(properties.isSecure())
                .path(properties.getPath())
                .sameSite(properties.getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public Optional<String> getAccessTokenFromCookies(HttpServletRequest request) {
        return getJwtFromCookies(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    public Optional<String> getRefreshTokenFromCookies(HttpServletRequest request) {
        return getJwtFromCookies(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    private Optional<String> getJwtFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
