package dev.ehutson.template.security;

import dev.ehutson.template.security.service.refreshtoken.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;
    private final JwtCookieManager cookieManager;
    private final RefreshTokenService refreshTokenService;

    // Paths that should skip session updates for performance
    private static final String[] SKIP_SESSION_UPDATE_PATHS = {
            "/actuator/health",
            "/actuator/prometheus",
            "/favicon.ico",
            "/static/"
    };

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        Optional<String> jwtToken = cookieManager.getAccessTokenFromCookies(request);

        if (jwtToken.isPresent()) {
            processJwtToken(jwtToken.get(), request, response);
        } else {
            log.error("No JWT token found in the request cookie, proceeding without authentication from cookie");
        }

        filterChain.doFilter(request, response);
    }

    private void processJwtToken(String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String username = jwt.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(username, request);

                // Update the session asynchronously if it is not a static resource
                if (shouldUpdateSession(request)) {
                    updateSessionAsync(jwt.getId());
                }
            }
        } catch (JwtException e) {
            handleJwtException(e, response);
        } catch (UsernameNotFoundException e) {
            handleUserNotFoundException(response);
        } catch (Exception e) {
            // Log only the essential information, never full stack traces in production
            log.error("Authentication failed for request: {} - Error: {}", request.getRequestURI(), e.getMessage());
            cookieManager.clearAccessTokenCookie(response);
        }
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Create the authentication token
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("User {} authenticated successfully", username);
    }

    private void updateSessionAsync(String sessionId) {
        // Update session access time asynchronously to avoid blocking the request
        CompletableFuture.runAsync(() -> {
            try {
                refreshTokenService.updateLastAccessed(sessionId);
            } catch (Exception e) {
                log.warn("Failed to update session last accessed time: {}", e.getMessage());
            }
        });
    }

    private boolean shouldUpdateSession(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String skipPath : SKIP_SESSION_UPDATE_PATHS) {
            if (path.startsWith(skipPath)) {
                return false;
            }
        }
        return true;
    }

    private void handleJwtException(JwtException e, HttpServletResponse response) {
        // Log minimal information - no stack traces or sensitive data
        if (e.getMessage().contains("expired")) {
            log.debug("JWT token expired");
        } else {
            log.debug("Invalid JWT token");
        }
        cookieManager.clearAccessTokenCookie(response);
    }

    private void handleUserNotFoundException(HttpServletResponse response) {
        log.warn("User not found during authentication");
        cookieManager.clearAccessTokenCookie(response);
    }
}
