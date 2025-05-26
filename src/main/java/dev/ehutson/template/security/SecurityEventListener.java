package dev.ehutson.template.security;

import dev.ehutson.template.monitoring.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static dev.ehutson.template.util.ServletRequestUtil.getRequest;

@Component
@RequiredArgsConstructor
public class SecurityEventListener {
    private final AuditService auditService;

    @EventListener
    public void handleAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
        Map<String, String> data = new HashMap<>();
        data.put(Constants.ACTION, Constants.LOGIN);
        data.put(Constants.STATUS, Constants.SUCCESS);

        HttpServletRequest request = getRequest();
        String principal = request.getUserPrincipal().getName();
        auditService.logEvent(principal, Constants.AUTHENTICATION, data, request);
    }

    @EventListener
    public void handleAuthenticationFailureEvent(AbstractAuthenticationFailureEvent event) {
        Map<String, String> data = new HashMap<>();
        data.put(Constants.ACTION, Constants.LOGIN);
        data.put(Constants.STATUS, Constants.FAILURE);
        data.put("reason", event.getException().getMessage());

        String principal = event.getAuthentication().getName();
        auditService.logEvent(principal, Constants.AUTHENTICATION, data, getRequest());
    }

    @EventListener
    public void handleLogoutSuccessEvent(LogoutSuccessEvent event) {
        Map<String, String> data = new HashMap<>();
        data.put(Constants.ACTION, Constants.LOGOUT);
        data.put(Constants.STATUS, Constants.SUCCESS);

        auditService.logEvent(event.getAuthentication().getName(),
                Constants.AUTHENTICATION, data, getRequest());
    }

}
