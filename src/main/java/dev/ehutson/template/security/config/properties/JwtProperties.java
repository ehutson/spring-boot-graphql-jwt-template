package dev.ehutson.template.security.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @NotNull(message = "JWT public key must be configured")
    private RSAPublicKey publicKey;

    @NotNull(message = "JWT private key must be configured")
    private RSAPrivateKey privateKey;

    private boolean secure = true;
    private boolean httpOnly = true;

    @Min(value = 60, message = "Access token expiration must be at least 60 seconds")
    private long accessTokenExpirationSeconds = 3600;

    @Min(value = 300, message = "Refresh token expiration must be at least 300 seconds")
    private long refreshTokenExpirationSeconds = 86400;

    @NotBlank(message = "Same site policy must be specified")
    private String sameSite = "Strict";

    @NotBlank(message = "Cookie path must be specified")
    private String path = "/";

    private String accessTokenCookieName = "access_token";
    private String refreshTokenCookieName = "refresh_token";

    // Security features
    private boolean fingerprintUserAgent = false;
    private boolean fingerprintIpAddress = false;

    @NotBlank(message = "Token issuer must be specified")
    private String issuer = "self";

    // New security configuration
    private SecurityConfig security = new SecurityConfig();

    @Data
    public static class SecurityConfig {
        // Cron expression for token cleanup - default to daily at midnight
        private String tokenCleanupCron = "0 0 0 * * ?";

        // Maximum number of active sessions per user (0 = unlimited)
        private int maxActiveSessionsPerUser = 0;

        // Enable token reuse detection
        private boolean enableTokenReuseDetection = true;

        // Token reuse window in seconds (how long to track old tokens)
        private long tokenReuseWindowSeconds = 3600;

        // Rate limiting for authentication attempts
        private RateLimitConfig rateLimit = new RateLimitConfig();
    }

    @Data
    public static class RateLimitConfig {
        private boolean enabled = true;
        private int maxAttemptsPerMinute = 5;
        private int maxAttemptsPerHour = 20;
        private long lockoutDurationMinutes = 15;
    }
}