package dev.ehutson.template.security;

import dev.ehutson.template.security.service.RefreshTokenService;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;
    private final JwtCookieManager cookieManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        Optional<String> jwtToken = cookieManager.getAccessTokenFromCookies(request);

        if (jwtToken.isPresent()) {
            try {
                Jwt jwt = jwtDecoder.decode(jwtToken.get());
                String username = jwt.getSubject();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
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

                    // Refresh session metadata (lastAccessedAt) in Mongo
                    String sessionId = jwt.getId();
                    refreshTokenService.updateLastAccessed(sessionId);
                }
            } catch (JwtException | UsernameNotFoundException e) {
                // Log error but don't throw, let subsequent filters handle unauthorized
                log.error("Cannot set user authentication: {}", e.getMessage(), e);
                cookieManager.clearAccessTokenCookie(response);
            }
        } else {
            log.error("No JWT token found, proceeding without authentication from cookie");
        }

        filterChain.doFilter(request, response);
    }


}
