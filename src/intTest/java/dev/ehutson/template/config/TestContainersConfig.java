package dev.ehutson.template.config;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * TestConfiguration for all container used in integration tests
 * Uses Spring Boot 3.1+ @ServiceConnection for automatic configuration
 */
@SuppressWarnings("resource")
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

    static final String MONGODB_IMAGE = "mongo:6.0.7";
    static final int MONGODB_PORT = 27017;

    static final String REDIS_IMAGE = "redis:7.2-alpine";
    static final int REDIS_PORT = 6379;

    static final String MAILHOG_IMAGE = "mailhog/mailhog:latest";
    static final int MAILHOG_SMTP_PORT = 1025;
    static final int MAILHOG_API_PORT = 8025;

    /**
     * Creates and configures a MongoDB container
     *
     * @return the MongoDB container
     */
    @Bean
    @ServiceConnection
    @RestartScope
    public MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse(MONGODB_IMAGE))
                .withExposedPorts(MONGODB_PORT)
                .waitingFor(Wait.forListeningPort());
    }

    /**
     * Creates and configures a Redis container
     *
     * @return the redis container
     */
    @Bean
    @ServiceConnection(name = "redis")
    @RestartScope
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT)
                .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));
    }

    /**
     * Creates and configures a MailHog container
     * Since there's no @ServiceConnection support for mail, we need to set properties manually
     *
     * @return the MailHog container
     */
    @Bean
    @RestartScope
    public GenericContainer<?> mailhogContainer() {
        return new GenericContainer<>(DockerImageName.parse(MAILHOG_IMAGE))
                .withExposedPorts(MAILHOG_SMTP_PORT, MAILHOG_API_PORT) // SMTP port, HTTP API/UI port
                .withEnv("MH_STORAGE", "memory")
                .waitingFor(Wait.forHttp("/api/v2/messages").forPort(MAILHOG_API_PORT));
    }

    /**
     * Creates a DynamicPropertyRegistrar to register properties for the MailHog container
     *
     * @param mailhogContainer The MailHog container
     * @return the property registrar
     */
    @Bean
    public DynamicPropertyRegistrar mailProperties(GenericContainer<?> mailhogContainer) {
        return registry -> {
            registry.add("spring.mail.host", mailhogContainer::getHost);
            registry.add("spring.mail.port", () -> mailhogContainer.getMappedPort(MAILHOG_SMTP_PORT));
            registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
            registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> false);
        };
    }
}
