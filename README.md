# Spring Boot GraphQL JWT Template

A production-ready Spring Boot application template with JWT authentication, GraphQL API, and MongoDB persistence. This template implements security best practices and modern architecture patterns, serving as an excellent foundation for new projects.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![GraphQL](https://img.shields.io/badge/GraphQL-DGS-blue)
![MongoDB](https://img.shields.io/badge/Database-MongoDB-green)
![JWT](https://img.shields.io/badge/Security-JWT-yellow)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

## 🌟 Key Features

- **Java 21** with modern language features
- **Spring Boot 3.4.4** with Spring Security 6
- **Netflix DGS GraphQL** for API design
- **JWT Authentication** with HTTP-only cookies
- **Advanced Session Management** with database-stored refresh tokens
- **MongoDB** as the primary database
- **Role-Based Authorization** (USER, ADMIN)
- **Stateless JWT** design with refresh token rotation
- **Docker** and **TestContainers** integration
- **Mongock** for database migrations
- **RSA Key-Pair** based token encryption
- **Gradle** build system with modern plugins

## 🏗️ Architecture

The application follows a clean, layered architecture:

```
├── Domain Layer       (Entity models)
├── Repository Layer   (Data access)
├── Service Layer      (Business logic)
├── GraphQL Layer      (API endpoints)
└── Security Layer     (Authentication & Authorization)
```

### Security Implementation

This template implements security best practices:

- **JWT stored in HTTP-only cookies** (not localStorage)
- **Refresh token rotation** for enhanced security
- **RSA-based token signatures** rather than shared secrets
- **Multiple device support** with session tracking
- **Method-level security** with Spring Security annotations

### Technology Stack

- **Backend**: Spring Boot, Spring Security, Spring Data MongoDB
- **API**: GraphQL with Netflix DGS Framework
- **Database**: MongoDB
- **Authentication**: JWT with cookie-based storage
- **Build Tool**: Gradle
- **Testing**: JUnit 5, TestContainers
- **Containerization**: Docker, Docker Compose

## 🚀 Getting Started

### Prerequisites

- JDK 21
- Docker and Docker Compose
- MongoDB (or use the provided Docker setup)
- Git

### Setup and Installation

1. **Clone the repository**

```bash
git clone https://github.com/ehutson/spring-boot-graphql-jwt-template.git
cd spring-boot-graphql-jwt-template
```

2. **Generate RSA keys for JWT**

```bash
# go to certs directory
cd src/main/resources/certs

# create rsa key pair
openssl genrsa -out keypair.pem 2048

# extract public key
openssl rsa -in keypair.pem -pubout -out public.pem

# create private key in PKCS#8 format
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem

# remove keypair.pem - it is no longer needed
rm keypair.pem
```

3. **Start MongoDB using Docker Compose**

```bash
docker-compose up -d mongodb
```

4. **Build and run the application**

```bash
./gradlew bootRun
```

5. **Access the GraphQL Playground**

Open your browser and navigate to:
```
http://localhost:8080/api/graphql
```

### Configuration

The application uses YAML-based configuration with profiles:

- `application.yml`: Base configuration
- `application-dev.yml`: Development environment settings
- `application-prod.yml`: Production environment settings

Key configuration options:

```yaml
jwt:
  access-token:
    expiration: 900000  # 15 minutes in milliseconds
  refresh-token:
    expiration: 604800000  # 7 days in milliseconds
  cookie:
    secure: true  # Requires HTTPS in production
    http-only: true
    same-site: lax
```

## 📝 API Documentation

### GraphQL Schema

The GraphQL schema defines the following main operations:

**Queries:**
- `me`: Get current user information
- `user(id)`: Get a specific user by ID
- `users`: List all users (admin only)
- `roles`: List available roles (admin only)
- `activeSessions`: List current user's active sessions

**Mutations:**
- `register(input)`: Register a new user
- `login(input)`: Authenticate a user
- `refreshToken`: Refresh the authentication token
- `logout`: Log out the current user
- `revokeAllSessions`: Revoke all active sessions for user

### Authentication Flow

1. **Registration**
   ```graphql
   mutation {
     register(input: {
       username: "user1",
       email: "user1@example.com",
       password: "password123"
     }) {
       success
       message
       user {
         id
         username
         email
       }
     }
   }
   ```

2. **Login**
   ```graphql
   mutation {
     login(input: {
       username: "user1",
       password: "password123"
     }) {
       success
       message
       user {
         id
         username
         email
       }
     }
   }
   ```

3. **Access Protected Resources**
   ```graphql
   query {
     me {
       id
       username
       email
       roles {
         name
       }
     }
   }
   ```

4. **Refresh Token**
   ```graphql
   mutation {
     refreshToken {
       success
       message
     }
   }
   ```

5. **Logout**
   ```graphql
   mutation {
     logout
   }
   ```

## 🧪 Testing

The project includes comprehensive test coverage:

```bash
./gradlew test
```

- **Unit Tests**: Test individual components
- **Integration Tests**: Test components with TestContainers MongoDB
- **Security Tests**: Verify authentication and authorization

## 🐳 Deployment

### Docker Deployment

Build and run the complete application:

```bash
./gradlew build
docker-compose up -d
```

### Production Considerations

For production deployment, remember to:

1. Set `jwt.cookie.secure=true` to require HTTPS
2. Configure proper MongoDB credentials
3. Set up proper logging configuration
4. Configure appropriate CORS settings
5. Generate new RSA keys for production

## 🛠️ Development

### Adding New Features

1. **Define GraphQL Schema**: Add types/queries/mutations to `schema.graphqls`
2. **Create Entity Models**: Add domain classes in the `domain` package
3. **Create Repositories**: Add repositories in the `repository` package
4. **Implement Services**: Add business logic in the `service` package
5. **Create DataFetchers**: Implement GraphQL resolvers in the `graphql.datafetcher` package

### Database Migrations

Database migrations are handled by Mongock:

1. Create a migration class in the `migration` package
2. Annotate it with `@ChangeUnit`
3. Implement the `execute` method

Example:
```java
@ChangeUnit(id = "add-new-role", order = "002")
public class AddNewRoleMigration {
    @Execution
    public void execute() {
        // Migration code
    }
}
```

## 📚 Further Documentation

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Netflix DGS Framework](https://netflix.github.io/dgs/)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [JWT.io](https://jwt.io/)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request