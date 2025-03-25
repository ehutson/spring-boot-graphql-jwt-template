package dev.ehutson.template.monitoring.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RedisHealthIndicator extends AbstractHealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            if ("PONG".equals(connection.ping())) {
                String info = Objects.requireNonNull(connection.serverCommands().info()).toString();
                builder.up()
                        .withDetail("version", parseRedisVersion(info))
                        .withDetail("clients", Objects.requireNonNull(connection.serverCommands().getClientList()).size());
            } else {
                builder.down().withDetail("error", "Redis ping failed");
            }
        } catch (Exception e) {
            builder.down(e);
        }
    }

    private String parseRedisVersion(String info) {
        for (String line : info.split("\n")) {
            if (line.startsWith("redis_version")) {
                return line.split(":")[1].trim();
            }
        }
        return "unknown";
    }
}
