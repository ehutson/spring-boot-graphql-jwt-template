package dev.ehutson.template.security.service.refreshtoken;

import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.repository.RefreshTokenRepository;
import dev.ehutson.template.security.config.properties.JwtProperties;
import dev.ehutson.template.security.fingerprint.FingerprintValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service responsible for validating refresh tokens.
 * Separated to avoid @Transactional method calls via "this".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenValidator {

    private final RefreshTokenRepository refreshTokenRepository;
    private final FingerprintValidator fingerprintValidator;

    @Transactional(readOnly = true)
    public RefreshTokenModel validateRefreshToken(String token, HttpServletRequest request, JwtProperties properties) {
        RefreshTokenModel storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> {
                    log.warn("Invalid or expired refresh token attempted.");
                    return ApplicationException.of(ErrorCode.TOKEN_EXPIRED, "Token expired or is invalid");
                });

        // Fingerprint validation
        if (!fingerprintValidator.validateFingerprint(storedToken, request, properties)) {
            throw ApplicationException.of(ErrorCode.VALIDATION_FAILED, "Token validation failed");
        }

        return storedToken;
    }
}