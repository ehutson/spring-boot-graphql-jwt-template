package dev.ehutson.template.security.service;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.exception.InvalidTokenException;
import dev.ehutson.template.exception.ResourceNotFoundException;
import dev.ehutson.template.monitoring.audit.AuditService;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.JwtCookieManager;
import dev.ehutson.template.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final JwtCookieManager cookieManager;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    public void authenticate(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        // Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(authentication);

        // Create a refresh token and save it to the database
        RefreshTokenModel refreshToken = refreshTokenService.createRefreshToken(userDetails.getId(), request);

        // Add cookies to the response
        cookieManager.addAccessTokenCookie(response, accessToken);
        cookieManager.addRefreshTokenCookie(response, refreshToken.getToken());

        log.debug("User {} authenticated successfully", username);

        // Log the successful authentication
        Map<String, String> auditData = new HashMap<>();
        auditData.put("action", "LOGIN");
        auditData.put("status", "SUCCESS");
        auditService.logEvent(username, "AUTHENTICATION", auditData, request);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // Get refresh token from cookie
        String refreshTokenString = getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh token not found"
                ));

        try {
            // Validate and rotate the refresh token
            RefreshTokenModel refreshToken = refreshTokenService.rotateRefreshToken(refreshTokenString, request);

            // Create a new authentication from the user details
            UserDetailsImpl userDetails = userRepository.findById(refreshToken.getUserId())
                    .map(UserDetailsImpl::build)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found",
                            "refreshToken", refreshToken.getUserId())
                    );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate new access token
            String newAccessToken = tokenProvider.generateAccessToken(authentication);

            // Add cookies to the response
            cookieManager.addAccessTokenCookie(response, newAccessToken);
            cookieManager.addRefreshTokenCookie(response, refreshToken.getToken());

            log.debug("Token refreshed successfully for user ID: {}", refreshToken.getUserId());
        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            logout(request, response);
            throw new InvalidTokenException("Unable to refresh token", e);
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "anonymous";

        // Revoke the refresh token if present
        getRefreshTokenFromCookie(request)
                .ifPresent(refreshTokenService::revokeRefreshToken);

        // Clear security context
        SecurityContextHolder.clearContext();

        // Clear cookies
        cookieManager.clearAccessTokenCookie(response);
        cookieManager.clearRefreshTokenCookie(response);

        log.debug("User logged out successfully");

        // Log the logout event
        Map<String, String> auditData = new HashMap<>();
        auditData.put("action", "LOGOUT");
        auditService.logEvent(username, "AUTHENTICATION", auditData, request);
    }

    public void revokeAllSessions(String userId, HttpServletResponse response) {
        refreshTokenService.revokeAllUserTokens(userId);

        // Clear current session
        SecurityContextHolder.clearContext();
        cookieManager.clearAccessTokenCookie(response);
        cookieManager.clearRefreshTokenCookie(response);

        log.debug("Revoked all sessions for user ID: {}", userId);
    }

    private Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> JwtCookieManager.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
        }

        return Optional.empty();
    }
}
