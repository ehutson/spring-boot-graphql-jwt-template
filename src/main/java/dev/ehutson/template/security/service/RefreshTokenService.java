package dev.ehutson.template.security.service;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.exception.TokenExpiredException;
import dev.ehutson.template.exception.ValidationFailedException;
import dev.ehutson.template.repository.RefreshTokenRepository;
import dev.ehutson.template.security.JwtTokenProvider;
import dev.ehutson.template.security.config.properties.JwtProperties;
import dev.ehutson.template.security.fingerprint.FingerprintValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;
    private final FingerprintValidator fingerprintValidator;

    @Transactional
    public RefreshTokenModel createRefreshToken(String userId, HttpServletRequest request) {
        String tokenString = tokenProvider.generateRefreshToken();

        RefreshTokenModel refreshToken = RefreshTokenModel.builder()
                .token(tokenString)
                .userId(userId)
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(getClientIP(request))
                .expiresAt(Instant.now().plusSeconds(properties.getRefreshTokenExpirationSeconds()))
                .createdAt(Instant.now())
                .revoked(false)
                .build();

        RefreshTokenModel saved = refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token for user: {}", userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public RefreshTokenModel validateRefreshToken(String token, HttpServletRequest request) {
        RefreshTokenModel storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> {
                    log.warn("Invalid or expired refresh token attempted.");
                    return new TokenExpiredException("Token expired or invalid");
                });

        // Fingerprint validation
        if (!fingerprintValidator.validateFingerprint(storedToken, request, properties)) {
            handleSuspiciousActivity(storedToken);
            throw new ValidationFailedException("Token validation failed");
        }

        return storedToken;
    }

    @Transactional
    public void updateLastAccessed(String sessionId) {
        refreshTokenRepository.findByToken(sessionId)
                .ifPresent(token -> {
                    token.setLastAccessedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                    log.debug("Revoked refresh token for user: {}", refreshToken.getUserId());
                });
    }

    @Transactional
    public void revokeAllUserTokens(String userId) {
        List<RefreshTokenModel> userTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);

        if (!userTokens.isEmpty()) {
            userTokens.forEach(token -> token.setRevoked(true));
            refreshTokenRepository.saveAll(userTokens);
            log.info("Revoked {} tokens for user: {}", userTokens.size(), userId);
        }
    }

    @Transactional
    public RefreshTokenModel rotateRefreshToken(String oldToken, HttpServletRequest request) {
        RefreshTokenModel existingToken = validateRefreshToken(oldToken, request);

        // Revoke the existing token
        existingToken.setRevoked(true);

        // Create a new token
        RefreshTokenModel newToken = createRefreshToken(existingToken.getUserId(), request);

        // Link the old token to the new one
        existingToken.setReplacedByToken(newToken.getToken());
        refreshTokenRepository.save(existingToken);

        log.debug("Rotated refresh token for user: {}", existingToken.getUserId());
        return newToken;
    }

    @Transactional(readOnly = true)
    public List<RefreshTokenModel> getUserActiveSessions(String userId) {
        return refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
    }

    @Scheduled(cron = "${app.security.token-cleanup-cron:0 0 0 * * ?}")
    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.debug("Purged expired refresh tokens");
    }

    private void handleSuspiciousActivity(RefreshTokenModel token) {
        // Log security event without exposing token details
        log.warn("Suspicious token activity detected for user: {} - Revoking all tokens", token.getUserId());

        // Optionally implement additional security measures:
        // - Send security alert to user
        // - Log to security monitoring system
        // - Increment failed attempt counter

        revokeAllUserTokens(token.getUserId());
    }

    private String getClientIP(HttpServletRequest request) {
        // Check for common proxy headers in order of preference
        String[] headers = {
                "X-Real-IP",
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated list of IPs
                int commaIdx = ip.indexOf(',');
                if (commaIdx > 0) {
                    return ip.substring(0, commaIdx).trim();
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }
}
