package dev.ehutson.template.monitoring.health;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoHealthIndicator extends AbstractHealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            Document result = mongoTemplate.executeCommand(new Document("ping", 1));
            if (result.getDouble("ok") == 1.0) {
                builder.up().withDetail("database", mongoTemplate.getDb().getName());

            } else {
                builder.down().withDetail("error", "MongoDB ping failed");
            }
        } catch (Exception e) {
            builder.down(e);
        }

    }
}
