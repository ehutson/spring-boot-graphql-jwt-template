package dev.ehutson.template.security;

import dev.ehutson.template.monitoring.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static dev.ehutson.template.util.ServletRequestUtil.getRequest;

@Component
@RequiredArgsConstructor
@Aspect
public class MethodSecurityAuditAspect {
    private final AuditService auditService;

    @Around("@annotation(preAuthorize)")
    public Object auditPreAuthorize(ProceedingJoinPoint joinPoint, PreAuthorize preAuthorize) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (AccessDeniedException e) {
            logMethodSecurityViolation(joinPoint, preAuthorize.value(), e);
            throw e;
        }
    }

    @Around("@annotation(postAuthorize)")
    public Object auditPostAuthorize(ProceedingJoinPoint joinPoint, PostAuthorize postAuthorize) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (AccessDeniedException e) {
            logMethodSecurityViolation(joinPoint, postAuthorize.value(), e);
            throw e;
        }
    }

    private void logMethodSecurityViolation(JoinPoint joinPoint, String expression, AccessDeniedException e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        Map<String, String> data = new HashMap<>();
        data.put("action", "METHOD_ACCESS_DENIED");
        data.put("method", Objects.requireNonNull(joinPoint.getThis()).getClass().getName());
        data.put("expression", expression);
        data.put("reason", e.getMessage());

        // Capture method parameters (be careful with sensitive data)
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            StringBuilder argsStr = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                // Just capture parameter types, not values for security
                argsStr.append("arg").append(i).append(": ")
                        .append(args[i] != null ? args[i].getClass().getSimpleName() : "null")
                        .append(", ");
            }
            data.put("parameters", argsStr.toString());
        }

        auditService.logEvent(username, "METHOD_SECURITY", data, getRequest());
    }
}
