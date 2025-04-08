package dev.ehutson.template.security;

import dev.ehutson.template.security.config.properties.JwtProperties;
import dev.ehutson.template.security.service.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtTokenProvider.
 * <p>
 * This test class verifies the functionality of the JwtTokenProvider using mocks,
 * focusing on the internal implementation details rather than end-to-end functionality.
 * <p>
 * The tests focus on:
 * 1. Generation of access tokens with correct claims (subject, userId, scope, expiration)
 * 2. Correct handling of multiple user authorities in token claims
 * 3. Edge case of generating tokens with no authorities
 * 4. Generation of refresh tokens with correct UUID format
 * 5. Verification that refresh tokens are unique across calls
 * <p>
 * These unit tests ensure that the JWT token provider creates tokens with the
 * correct structure and content, mocking external dependencies like JwtEncoder.
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Captor
    private ArgumentCaptor<JwtEncoderParameters> parametersCaptor;

    private final String testTokenValue = "test.jwt.token";


    /**
     * Tests the basic generation of JWT access tokens.
     * <p>
     * This test verifies that:
     * 1. The token is successfully generated with the expected value
     * 2. The token contains the correct claims (subject, scope, userId, issuer)
     * 3. The token has the correct issue and expiration times
     * 4. The expiration time is set to the configured value (3600 seconds)
     */
    @Test
    void testGenerateAccessToken() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtProperties.getIssuer()).thenReturn("self");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(testTokenValue);
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id("user123")
                .username("testuser")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .enabled(true)
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Act
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // Assert
        assertEquals(testTokenValue, token);
        verify(jwtEncoder).encode(parametersCaptor.capture());

        // Extract claims directly without assertions that might fail
        Map<String, Object> claims = parametersCaptor.getValue().getClaims().getClaims();

        // Verify claims exist and have expected values
        assertEquals("testuser", claims.get("sub"));
        assertEquals(1, ((List<String>) claims.get("scope")).size());
        assertEquals("ROLE_USER", ((List<String>) claims.get("scope")).getFirst());
        assertEquals("user123", claims.get("userId"));
        assertEquals("self", claims.get("iss"));

        // Verify times
        Instant now = Instant.now();
        Instant issuedAt = (Instant) claims.get("iat");
        Instant expiresAt = (Instant) claims.get("exp");

        // Ensure timestamps exist
        assertNotNull(issuedAt);
        assertNotNull(expiresAt);

        // Check timing is logical
        assertTrue(issuedAt.isBefore(now.plusSeconds(10))); // Allow some buffer
        assertTrue(expiresAt.isAfter(now));

        // Verify expiration time is roughly 3600 seconds after issuance
        long diffSeconds = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
        assertTrue(Math.abs(diffSeconds - 3600) < 10); // Allow some buffer
    }

    /**
     * Tests the generation of JWT access tokens with multiple authorities.
     * <p>
     * This test verifies that when a user has multiple roles, all of those roles
     * are correctly included in the token's scope claim.
     */
    @Test
    void testGenerateAccessToken_WithMultipleAuthorities() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtProperties.getIssuer()).thenReturn("self");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(testTokenValue);
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id("user123")
                .username("testuser")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                ))
                .enabled(true)
                .build();

        Authentication authWithMultipleRoles = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Act
        jwtTokenProvider.generateAccessToken(authWithMultipleRoles);

        // Assert
        verify(jwtEncoder).encode(parametersCaptor.capture());
        Map<String, Object> claims = parametersCaptor.getValue().getClaims().getClaims();

        // Verify the scope contains both roles
        List<String> scope = (List<String>) claims.get("scope");
        assertTrue(scope.contains("ROLE_USER"));
        assertTrue(scope.contains("ROLE_ADMIN"));
    }

    /**
     * Tests the generation of JWT access tokens with no authorities.
     * <p>
     * This test verifies that when a user has no roles, the token's scope claim
     * is set to an empty string rather than null or omitted entirely.
     */
    @Test
    void testGenerateAccessToken_WithNoAuthorities() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtProperties.getIssuer()).thenReturn("self");


        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(testTokenValue);
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id("user123")
                .username("testuser")
                .authorities(Collections.emptyList())
                .enabled(true)
                .build();

        Authentication authWithNoRoles = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Act
        jwtTokenProvider.generateAccessToken(authWithNoRoles);

        // Assert
        verify(jwtEncoder).encode(parametersCaptor.capture());
        Map<String, Object> claims = parametersCaptor.getValue().getClaims().getClaims();

        // Verify the scope is an empty string
        assertEquals(0, ((List<String>) claims.get("scope")).size());
    }

    /**
     * Tests the generation of refresh tokens.
     * <p>
     * This test verifies that:
     * 1. The refresh token is successfully generated and not empty
     * 2. The token has the correct UUID format
     * 3. The JwtEncoder is not used for refresh tokens (which are plain UUIDs)
     */
    @Test
    void testGenerateRefreshToken() {
        // Act
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        // Check that it's a valid UUID
        assertTrue(refreshToken.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));

        // Verify that no interactions with the encoder (refresh tokens are plain UUIDs)
        verifyNoInteractions(jwtEncoder);
    }

    /**
     * Tests that refresh tokens are unique across multiple calls.
     * <p>
     * This test verifies that consecutive calls to generateRefreshToken()
     * return different token values, ensuring uniqueness of refresh tokens.
     */
    @Test
    void testGenerateRefreshToken_ReturnsDifferentTokens() {
        // Act
        String refreshToken1 = jwtTokenProvider.generateRefreshToken();
        String refreshToken2 = jwtTokenProvider.generateRefreshToken();

        // Assert
        assertNotEquals(refreshToken1, refreshToken2, "Refresh tokens should be unique");

        // Verify that no interactions with the encoder
        verifyNoInteractions(jwtEncoder, jwtProperties);
    }
}