package dev.ehutson.template;

import com.mongodb.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainers configuration for integration tests.
 * Configures a MongoDB container for tests and overrides
 * any local MongoDB connection.
 */
@Configuration
@TestConfiguration
public class TestContainersConfiguration {
    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:6-jammy");

    private static final MongoDBContainer mongoContainer;

    static {
        mongoContainer = new MongoDBContainer(MONGO_IMAGE)
                .withExposedPorts(27017);
        mongoContainer.start();

        // Set system property to force using TestContainers MongoDB
        System.setProperty("spring.data.mongodb.uri", mongoContainer.getReplicaSetUrl());
    }

    @Bean
    public MongoDBContainer mongoDBContainer() {
        return mongoContainer;
    }

    /**
     * Set MongoDB connection properties for all tests
     */
    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    /**
     * Create a primary MongoTemplate that uses the TestContainers MongoDB
     * This ensures that the TestContainers MongoDB is used instead of a local MongoDB
     */
    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        MongoDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(
                MongoClients.create(mongoContainer.getReplicaSetUrl()),
                "test"
        );
        return new MongoTemplate(factory);
    }
}
