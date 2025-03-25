package dev.ehutson.template.security;

import dev.ehutson.template.monitoring.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static dev.ehutson.template.util.ServletRequestUtil.getRequest;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAuditAspect {
    private final AuditService auditService;

    @AfterThrowing(
            pointcut = "@annotation(org.springframework.security.access.prepost.PreAuthorize) || " +
                    "@annotation(org.springframework.security.access.prepost.PostAuthorize)",
            throwing = "exception"
    )
    public void logMethodAccessDenied(JoinPoint joinpoint, Exception exception) {
        if (exception instanceof AccessDeniedException) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";

            Map<String, String> data = new HashMap<>();
            data.put("action", "METHOD_ACCESS_DENIED");
            data.put("method", joinpoint.getSignature().toString());
            data.put("reason", exception.getMessage());

            auditService.logEvent(username, "METHOD_SECURITY", data, getRequest());
        }
    }
}
