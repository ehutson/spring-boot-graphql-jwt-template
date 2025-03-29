# Spring Boot GraphQL JWT Template - Developer Guide

## Build & Test Commands
- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- All tests: `./gradlew test`
- Integration tests: `./gradlew intTest`
- Single test class: `./gradlew test --tests "dev.ehutson.template.security.service.AuthorizationServiceTest"`
- Single test method: `./gradlew test --tests "dev.ehutson.template.security.service.AuthorizationServiceTest.testMethodName"`
- Coverage report: `./gradlew jacocoTestReport`

## Code Style Guidelines
- **Java version**: Java 21
- **Naming**: Classes (PascalCase with type suffix), methods (camelCase, verb-first), constants (UPPER_SNAKE_CASE)
- **Formatting**: 4-space indentation, logical grouping of methods
- **Import organization**: No wildcards, group by packages
- **DI**: Use constructor injection with Lombok's @RequiredArgsConstructor
- **Error handling**: Custom exceptions with error codes, centralized handling via GlobalExceptionHandler
- **Testing**: JUnit 5, Mockito, TestContainers, clear test naming (test<Feature>_<Scenario>)
- **Architecture**: Domain-driven design with separation of concerns (domain, service, repository, config)
- **Security**: JWT-based authentication, role-based authorization, secure exception handling