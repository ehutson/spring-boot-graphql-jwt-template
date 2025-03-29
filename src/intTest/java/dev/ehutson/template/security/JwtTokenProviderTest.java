package dev.ehutson.template.security;

import dev.ehutson.template.IntTestConfiguration;
import dev.ehutson.template.TemplateApplication;
import dev.ehutson.template.TestContainersConfiguration;
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

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TemplateApplication.class)
@Import({TestContainersConfiguration.class, IntTestConfiguration.class})
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtDecoder jwtDecoder;

    private Authentication authentication;

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