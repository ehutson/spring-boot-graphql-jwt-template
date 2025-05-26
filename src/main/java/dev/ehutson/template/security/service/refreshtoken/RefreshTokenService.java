package dev.ehutson.template.security.service.refreshtoken;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.security.config.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenValidator validator;
    private final RefreshTokenManager manager;
    private final JwtProperties properties;

    public RefreshTokenModel createRefreshToken(String userId, HttpServletRequest request) {
        return manager.createRefreshToken(userId, request, properties);
    }

    public RefreshTokenModel validateRefreshToken(String token, HttpServletRequest request) {
        return validator.validateRefreshToken(token, request, properties);
    }

    public void updateLastAccessed(String sessionId) {
        manager.updateLastAccessed(sessionId);
    }

    public void revokeRefreshToken(String token) {
        manager.revokeRefreshToken(token);
    }

    public void revokeAllUserTokens(String userId) {
        manager.revokeAllUserTokens(userId);
    }

    public List<RefreshTokenModel> getUserActiveSessions(String userId) {
        return manager.getUserActiveSessions(userId);
    }

    @Transactional
    public RefreshTokenModel rotateRefreshToken(String oldToken, HttpServletRequest request) {
        RefreshTokenModel existingToken = validateRefreshToken(oldToken, request);

        // Revoke the existing token
        existingToken.setRevoked(true);

        // Create a new token
        RefreshTokenModel newToken = createRefreshToken(existingToken.getUserId(), request);

        // Link the old token to the new one (separate transaction)
        manager.linkReplacementToken(oldToken, newToken.getToken());

        log.debug("Rotated refresh token for user: {}", existingToken.getUserId());
        return newToken;
    }


    @Scheduled(cron = "${app.security.token-cleanup-cron:0 0 0 * * ?}")
    public void purgeExpiredTokens() {
        manager.purgeExpiredTokens();
    }

    public void handleSuspiciousActivity(String tokenString, HttpServletRequest request) {
        try {
            RefreshTokenModel token = this.validateRefreshToken(tokenString, request);

            // Log security event without exposing token details
            log.warn("Suspicious token activity detected for userId: {}", token.getUserId());

            // Optionally implement additional security measures:
            // - Send security alert to user
            // - Log to security monitoring system
            // - Increment failed attempt counter

            manager.revokeAllUserTokens(token.getUserId());
        } catch (Exception e) {
            // Log security event without exposing token details
            log.warn("Suspicious token activity detected with unknown user or invalid token: {}", tokenString);
        }
    }
}
