package dev.ehutson.template;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfiguration {
    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:6-jammy");

    @Bean(destroyMethod = "stop")
    @Profile("test")
    public static MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer(MONGO_IMAGE)
                .withExposedPorts(27017);
        container.start();
        return container;
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        try (MongoDBContainer container = mongoDBContainer()) {
            registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
        }
    }
}
