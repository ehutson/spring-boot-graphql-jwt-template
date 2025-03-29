package dev.ehutson.template;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuration for integration tests
 * <p>
 * IMPORTANT: This is a configuration class, NOT a Spring Boot application.
 * It should be imported into test classes using @Import.
 * <p>
 * This configuration:
 * 1. Disables the default MongoAutoConfiguration to ensure TestContainers MongoDB is used
 * 2. Provides beans needed for integration testing
 * 3. Sets the test profile
 * 4. Imports TestCacheConfig to disable caching during tests
 * 5. Imports MockRedisConfiguration to avoid Redis connection issues
 */
@TestConfiguration
@Import({TestContainersConfiguration.class})
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
public class IntTestConfiguration {

}