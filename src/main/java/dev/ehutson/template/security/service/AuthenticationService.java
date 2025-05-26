package dev.ehutson.template.security.service;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.monitoring.audit.AuditService;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.Constants;
import dev.ehutson.template.security.JwtCookieManager;
import dev.ehutson.template.security.JwtTokenProvider;
import dev.ehutson.template.security.service.refreshtoken.RefreshTokenService;
import graphql.ErrorType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void authenticate(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
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


            auditLoginSuccess(username, request);
        } catch (BadCredentialsException e) {
            auditLoginFailure(username, request, "Invalid credentials");
            throw e;
        }
    }

    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // Get refresh token from cookie
        String refreshTokenString = getRefreshTokenFromCookie(request)
                .orElseThrow(() -> ApplicationException.of(ErrorCode.INVALID_TOKEN, "Refresh token not found"));

        try {
            // Validate and rotate the refresh token
            RefreshTokenModel refreshToken = refreshTokenService.rotateRefreshToken(refreshTokenString, request);

            // Create a new authentication from the user details
            UserDetailsImpl userDetails = userRepository.findById(refreshToken.getUserId())
                    .map(UserDetailsImpl::build)
                    .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND,
                            "User not found", "refreshToken", refreshToken.getUserId()));

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

            // If you can extract user info, handle suspicious activity
            refreshTokenService.handleSuspiciousActivity(refreshTokenString, request);

            performLogout(response);
            throw ApplicationException.of(ErrorCode.INVALID_TOKEN, "Unable to refresh token", e);
        }
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Get the current user for audit logging
        String username = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "anonymous";

        // Attempt to revoke the refresh token if present
        getRefreshTokenFromCookie(request)
                .ifPresent(token -> {
                    try {
                        refreshTokenService.revokeRefreshToken(token);
                    } catch (Exception e) {
                        // Log the error but don't fail the logout
                        log.warn("Failed to revoke refresh token during logout: {}", e.getMessage());
                    }
                });

        // Clear cookies and security context
        performLogout(response);

        log.debug("User {} logged out successfully", username);

        auditLogout(username, request);
    }

    @Transactional
    public void revokeAllSessions(String userId, HttpServletResponse response) {
        try {
            refreshTokenService.revokeAllUserTokens(userId);
            performLogout(response);
            log.debug("Revoked all sessions for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to revoke all sessions for user: {}", userId);
            throw ApplicationException.of(ErrorCode.INVALID_TOKEN, "Failed to revoke sessions", ErrorType.ValidationError, e);
        }
    }

    private void performLogout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        cookieManager.clearAccessTokenCookie(response);
        cookieManager.clearRefreshTokenCookie(response);
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

    private void auditLoginSuccess(String username, HttpServletRequest request) {
        Map<String, String> auditData = new HashMap<>();
        auditData.put(Constants.ACTION, Constants.LOGIN);
        auditData.put(Constants.STATUS, "SUCCESS");
        auditService.logEvent(username, Constants.AUTHENTICATION, auditData, request);
    }

    private void auditLoginFailure(String username, HttpServletRequest request, String reason) {
        Map<String, String> auditData = new HashMap<>();
        auditData.put(Constants.ACTION, Constants.LOGIN);
        auditData.put(Constants.STATUS, Constants.FAILURE);
        auditData.put(Constants.REASON, reason);
        auditService.logEvent(username, Constants.AUTHENTICATION, auditData, request);
    }

    private void auditLogout(String username, HttpServletRequest request) {
        Map<String, String> auditData = new HashMap<>();
        auditData.put(Constants.ACTION, Constants.LOGOUT);
        auditService.logEvent(username, Constants.AUTHENTICATION, auditData, request);
    }
}
