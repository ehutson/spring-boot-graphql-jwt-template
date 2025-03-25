package dev.ehutson.template.monitoring.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_events")
public class AuditEvent {
    @Id
    private String id;

    private String principal;
    private String type;
    private Map<String, String> data;
    private Instant timestamp;
    private String ipAddress;
    private String userAgent;
}
