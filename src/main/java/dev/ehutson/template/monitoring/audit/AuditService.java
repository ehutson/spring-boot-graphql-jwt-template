package dev.ehutson.template.monitoring.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final MongoTemplate mongoTemplate;

    public void logEvent(String principal, String type, Map<String, String> data, HttpServletRequest request) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .principal(principal)
                    .type(type)
                    .data(data)
                    .timestamp(Instant.now())
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            mongoTemplate.save(event);
            log.info("Audit event logged: {}", event);
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
