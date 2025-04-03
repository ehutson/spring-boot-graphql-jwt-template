package dev.ehutson.template.security;

import dev.ehutson.template.config.TestContainersConfig;
import dev.ehutson.template.security.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JwtTokenProvider.
 * <p>
 * This test class verifies the functionality of the JwtTokenProvider which
 * is responsible for generating and validating JWT tokens used for authentication.
 * <p>
 * The tests focus on:
 * 1. Generation of access tokens with correct claims and expiration
 * 2. Verification that tokens can be decoded with the configured JwtDecoder
 * 3. Generation of refresh tokens with correct format (UUID)
 * <p>
 * These tests ensure that the JWT token generation and validation work properly
 * with the actual Spring Security JWT implementation rather than using mocks.
 */
@Testcontainers
@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtDecoder jwtDecoder;

    private Authentication authentication;

    /**
     * Sets up the test environment before each test.
     * This method creates a test authentication object with UserDetailsImpl
     * containing test user data and authorities.
     * <p>
     * The test user is created with:
     * - ID: "testId"
     * - Username: "testUsername"
     * - Email: "test@test.com"
     * - Authority: "ROLE_USER"
     */
    @BeforeEach
    void setUp() {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id("testId")
                .username("testUsername")
                .email("test@test.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .enabled(true)
                .build();

        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * Tests the generation of JWT access tokens.
     * <p>
     * This test verifies that:
     * 1. The token is successfully generated and not empty
     * 2. The token can be decoded with the configured JwtDecoder
     * 3. The token contains the correct claims (subject, userId, scope)
     * 4. The token has an expiration date set in the future
     */
    @Test
    void testGenerateAccessToken() {
        // Generate the token
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // Token should not be null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Decode token
        Jwt jwt = jwtDecoder.decode(token);

        // Check claims
        assertEquals("testUsername", jwt.getSubject());
        assertEquals("testId", jwt.getClaim("userId"));
        assertEquals("ROLE_USER", jwt.getClaim("scope"));

        // Check expiration
        Instant exp = jwt.getExpiresAt();
        assertNotNull(exp);

        // Expiration should be in the future
        assertTrue(exp.isAfter(Instant.now()));
    }

    /**
     * Tests the generation of refresh tokens.
     * <p>
     * This test verifies that:
     * 1. The refresh token is successfully generated and not empty
     * 2. The token has the correct format (UUID format)
     * 3. The token has the expected length of 36 characters
     */
    @Test
    void testGenerateRefreshToken() {
        // Generate token
        String token = jwtTokenProvider.generateRefreshToken();

        // Token should not be null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());

        //  Should be a UUID
        assertEquals(36, token.length());
        assertTrue(token.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }
}