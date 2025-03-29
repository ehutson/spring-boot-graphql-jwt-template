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

    @Test
    void testGenerateAccessToken() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);

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
        assertEquals("ROLE_USER", claims.get("scope"));
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

    @Test
    void testGenerateAccessToken_WithMultipleAuthorities() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);

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
        String scope = (String) claims.get("scope");
        assertTrue(scope.contains("ROLE_USER"));
        assertTrue(scope.contains("ROLE_ADMIN"));
    }

    @Test
    void testGenerateAccessToken_WithNoAuthorities() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);

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
        assertEquals("", claims.get("scope"));
    }

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