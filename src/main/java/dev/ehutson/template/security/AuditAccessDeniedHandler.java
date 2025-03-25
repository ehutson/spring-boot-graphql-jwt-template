package dev.ehutson.template.security;

import dev.ehutson.template.monitoring.audit.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditAccessDeniedHandler implements AccessDeniedHandler {
    private final AuditService auditService;


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        Map<String, String> data = new HashMap<>();
        data.put("action", "ACCESS_DENIED");
        data.put("uri", request.getRequestURI());
        data.put("method", request.getMethod());
        data.put("reason", accessDeniedException.getMessage());

        auditService.logEvent(username, "AUTHORIZATION", data, request);

        // Set response status
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Access denied: " +
                accessDeniedException.getMessage() + "\"}");
    }
}
