package dev.ehutson.template.security.service.refreshtoken;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.repository.RefreshTokenRepository;
import dev.ehutson.template.security.JwtTokenProvider;
import dev.ehutson.template.security.config.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


/**
 * Service responsible for managing refresh token CRUD operations.
 * Separated to avoid @Transactional method calls via "this".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenManager {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public RefreshTokenModel createRefreshToken(String userId, HttpServletRequest request, JwtProperties properties) {
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
    public void updateLastAccessed(String sessionId) {
        refreshTokenRepository.findByToken(sessionId)
                .ifPresent(token -> {
                    token.setLastAccessedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional(readOnly = true)
    public List<RefreshTokenModel> getUserActiveSessions(String userId) {
        return refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
    }

    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.debug("Purged expired refresh tokens");
    }

    @Transactional
    public void linkReplacementToken(String oldToken, String newToken) {
        refreshTokenRepository.findByToken(oldToken)
                .ifPresent(token -> {
                    token.setReplacedByToken(newToken);
                    refreshTokenRepository.save(token);
                });
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