package dev.ehutson.template.security;

import dev.ehutson.template.security.config.properties.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtCookieManagerTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtCookieManager cookieManager;

    private ArgumentCaptor<String> headerCaptor;
    private final String testToken = "test-jwt-token";

    @BeforeEach
    void setUp() {
        headerCaptor = ArgumentCaptor.forClass(String.class);
    }

    @Test
    void testAddAccessTokenCookie() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtProperties.isHttpOnly()).thenReturn(true);
        when(jwtProperties.isSecure()).thenReturn(true);
        when(jwtProperties.getPath()).thenReturn("/");
        when(jwtProperties.getSameSite()).thenReturn("Strict");

        // Act
        cookieManager.addAccessTokenCookie(response, testToken);

        // Assert
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookie = headerCaptor.getValue();

        // Check cookie attributes
        assertTrue(cookie.startsWith(JwtCookieManager.ACCESS_TOKEN_COOKIE_NAME + "=" + testToken));
        assertTrue(cookie.contains("Max-Age=3600"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Strict"));
    }

    @Test
    void testAddRefreshTokenCookie() {
        // Arrange
        when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(86400L);
        when(jwtProperties.isHttpOnly()).thenReturn(true);
        when(jwtProperties.isSecure()).thenReturn(true);
        when(jwtProperties.getPath()).thenReturn("/");
        when(jwtProperties.getSameSite()).thenReturn("Strict");

        // Act
        cookieManager.addRefreshTokenCookie(response, testToken);

        // Assert
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookie = headerCaptor.getValue();

        // Check cookie attributes
        assertTrue(cookie.startsWith(JwtCookieManager.REFRESH_TOKEN_COOKIE_NAME + "=" + testToken));
        assertTrue(cookie.contains("Max-Age=86400"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Strict"));
    }

    @Test
    void testClearAccessTokenCookie() {
        // Arrange
        when(jwtProperties.isHttpOnly()).thenReturn(true);
        when(jwtProperties.isSecure()).thenReturn(true);
        when(jwtProperties.getPath()).thenReturn("/");
        when(jwtProperties.getSameSite()).thenReturn("Strict");

        // Act
        cookieManager.clearAccessTokenCookie(response);

        // Assert
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookie = headerCaptor.getValue();

        // Check cookie attributes indicating it's being cleared
        assertTrue(cookie.startsWith(JwtCookieManager.ACCESS_TOKEN_COOKIE_NAME + "="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("Path=/"));
    }

    @Test
    void testClearRefreshTokenCookie() {
        // Arrange
        when(jwtProperties.isHttpOnly()).thenReturn(true);
        when(jwtProperties.isSecure()).thenReturn(true);
        when(jwtProperties.getPath()).thenReturn("/");
        when(jwtProperties.getSameSite()).thenReturn("Strict");

        // Act
        cookieManager.clearRefreshTokenCookie(response);

        // Assert
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookie = headerCaptor.getValue();

        // Check cookie attributes indicating it's being cleared
        assertTrue(cookie.startsWith(JwtCookieManager.REFRESH_TOKEN_COOKIE_NAME + "="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("Path=/"));
    }

    @Test
    void testDifferentJwtPropertiesApplied() {
        // Arrange
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(1800L);
        when(jwtProperties.isHttpOnly()).thenReturn(false);
        when(jwtProperties.isSecure()).thenReturn(false);
        when(jwtProperties.getPath()).thenReturn("/api");
        when(jwtProperties.getSameSite()).thenReturn("Lax");

        // Act
        cookieManager.addAccessTokenCookie(response, testToken);

        // Assert
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookie = headerCaptor.getValue();

        assertTrue(cookie.contains("Max-Age=1800"));
        assertTrue(cookie.contains("Path=/api"));
        assertFalse(cookie.contains("Secure"));
        assertFalse(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
    }
}