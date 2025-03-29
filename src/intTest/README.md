# Integration Tests

This directory contains integration tests for the application that require a running environment with TestContainers.

## IntelliJ IDEA Setup

If you're experiencing the error "Autowired members must be defined in a valid Spring bean" in IntelliJ, follow these steps:

1. Run `./gradlew configureIntellij` for detailed setup instructions
2. In IntelliJ, go to File > Project Structure
3. Go to Modules > spring-boot-graphql-jwt-template > Sources
4. Mark src/intTest/java as "Test Sources"
5. Mark src/intTest/resources as "Test Resources"
6. Apply and OK
7. In IntelliJ, right-click on the intTest directory
8. Select "Mark Directory as" > "Test Sources Root"
9. Restart IntelliJ

## Running Tests

Integration tests can be run with:

```
./gradlew intTest
```

These tests use TestContainers to spin up MongoDB and other dependencies automatically.

## Test Configuration

Tests use the `application-test.yml` configuration under `src/intTest/resources/config` and have the "test" profile active.

The `TestContainersConfiguration` class initializes the test containers used by the integration tests.

## MongoDB TestContainers Configuration

The tests are configured to always use the MongoDB TestContainer instance, regardless of whether there's a local MongoDB running. This is accomplished through these mechanisms:

1. The `TestContainersConfiguration` class:
   - Creates a MongoDB container
   - Sets system properties to use the container's connection string
   - Registers a primary MongoTemplate bean that uses the TestContainer

2. The `IntTestConfiguration` class:
   - Excludes Spring Boot's default MongoDB auto-configuration
   - Provides transaction support using the TestContainer

3. The Gradle task:
   - Forces the "test" profile
   - Sets an empty MongoDB URI to ensure the TestContainer's URI takes precedence

This ensures that all tests will use the isolated TestContainer MongoDB instance rather than connecting to any local MongoDB server.

# Important Configuration Notes

If you see the error `Found multiple @SpringBootConfiguration annotated classes`, it means you have multiple classes with `@SpringBootApplication` in your test context. To fix this:

1. Always use `@SpringBootTest(classes = TemplateApplication.class)` in your test classes to explicitly specify which application class to use

2. Do not annotate test-specific classes like `TestTemplateApplication` with `@SpringBootApplication`

3. Keep the `TestTemplateApplication` as a simple configuration or launcher class

## Cache Configuration

The integration tests are configured to disable caching through multiple mechanisms:

1. The `TestCacheConfig` class provides a no-operation cache manager that doesn't actually cache anything

2. The application-test.yml explicitly sets `spring.cache.type=none`

3. Redis auto-configuration is excluded for tests

4. The main `CacheConfig` class is annotated with `@Profile("!test")` so it's not loaded during tests

5. A `MockRedisConfiguration` provides mock Redis beans to satisfy any remaining dependencies

This comprehensive approach ensures that tests don't interfere with each other due to cached data, don't require a Redis server, and won't encounter connection issues due to missing Redis.

# Cleanup

After running the tests, TestContainers should automatically clean up the containers. If you need to manually clean up:

```
./cleanIntTests.sh
```

This script will remove any lingering TestContainers, volumes, and networks.