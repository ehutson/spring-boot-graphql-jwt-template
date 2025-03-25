package dev.ehutson.template.security.service;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.exception.CustomException;
import dev.ehutson.template.repository.RefreshTokenRepository;
import dev.ehutson.template.security.JwtTokenProvider;
import dev.ehutson.template.security.config.properties.JwtProperties;
import graphql.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;

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
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenModel validateRefreshToken(String token, HttpServletRequest request) {
        RefreshTokenModel storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new CustomException(
                        "Invalid or expired refresh token",
                        "INVALID_TOKEN",
                        ErrorType.ValidationError
                ));

        // Fingerprint validation

        if (properties.isFingerprintUserAgent()) {

            // Basic validation - user agent must match
            String currentUserAgent = request.getHeader("User-Agent");
            if (!storedToken.getUserAgent().equals(currentUserAgent)) {
                // Log potential token theft attempt
                log.warn("Refresh token used with different user agent. Token: {}, User ID: {}",
                        token.substring(0, 6) + "...", storedToken.getUserId());
                revokeAllUserTokens(storedToken.getUserId());
                throw new CustomException(
                        "Security validation failed",
                        "SECURITY_ERROR",
                        ErrorType.ValidationError
                );
            }
        }

        if (properties.isFingerprintIpAddress()) {
            // Optional: IP validation with some flexibility (first 2 octets)
            // This allows for mobile network changes while still providing some security
            String currentIpAddress = getClientIP(request);
            String storedIpPrefix = getIpPrefix(storedToken.getIpAddress());
            String currentIpPrefix = getIpPrefix(currentIpAddress);

            if (!storedIpPrefix.equals(currentIpPrefix)) {
                // Consider risk level - maybe just log this or implement stricter checks
                log.warn("Refresh token used with different IP network. Token: {}, User ID: {}",
                        token.substring(0, 6) + "...", storedToken.getUserId());

                // Maybe increment a counter for suspicious activity instead of immediate revocation
            }
        }

        return storedToken;
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.debug("Revoked refresh token: {}", token);
        });
    }

    public void revokeAllUserTokens(String userId) {
        List<RefreshTokenModel> userTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);

        if (!userTokens.isEmpty()) {
            userTokens.forEach(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });

            log.debug("Revoked all tokens for user: {}", userId);
        }
    }

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

    public List<RefreshTokenModel> getUserActiveSessions(String userId) {
        return refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.debug("Purged expired refresh tokens");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getIpPrefix(String ipAddress) {
        // Extract just the first two octets for a network prefix
        // e.g. 192.168.1.1 -> 192.168
        String[] parts = ipAddress.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        // Fallback for IPv6 or unusual formats
        return ipAddress;
    }
}
