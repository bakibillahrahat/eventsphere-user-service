# EventSphere User Service

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)

## Overview

The User Service is a core microservice of the EventSphere platform responsible for user management, authentication, and authorization. It provides secure user registration, login functionality, and role-based access control using JWT tokens.

## Features

### 🔐 Authentication & Authorization
- User registration with email verification
- Secure login with JWT token generation
- Role-based access control (USER, ORGANIZER, ADMIN)
- Password encryption using BCrypt
- Token-based stateless authentication

### 👤 User Management
- User profile creation and management
- Email uniqueness validation
- User information retrieval
- Account status management

### 🛡️ Security Features
- Input validation with Bean Validation
- Global exception handling
- CORS configuration
- SQL injection prevention with JPA

### 📊 Additional Features
- Comprehensive logging
- Health check endpoints
- API documentation ready
- Docker containerization
- Service discovery integration

## Tech Stack

- **Framework**: Spring Boot 3.2
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA
- **Validation**: Bean Validation (Hibernate Validator)
- **Documentation**: OpenAPI 3 (Swagger)
- **Containerization**: Docker
- **Service Discovery**: Eureka Client
- **Build Tool**: Maven
- **Java Version**: 17

## API Endpoints

### Authentication Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/users/register` | Register new user | No |
| POST | `/api/users/login` | User login | No |

### User Management Endpoints
| Method | Endpoint | Description | Auth Required | Roles |
|--------|----------|-------------|---------------|--------|
| GET | `/api/users/{id}` | Get user by ID | Yes | USER/ORGANIZER/ADMIN (own profile) or ADMIN |
| GET | `/api/users/profile` | Get current user profile | Yes | USER/ORGANIZER/ADMIN |
| PUT | `/api/users/profile` | Update user profile | Yes | USER/ORGANIZER/ADMIN |
| GET | `/api/users` | Get all users (paginated) | Yes | ADMIN |
| DELETE | `/api/users/{id}` | Delete user | Yes | ADMIN |

### Health & Monitoring
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Health check |
| GET | `/actuator/info` | Service information |

## Quick Start

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **PostgreSQL** (if running locally without Docker)

### Option 1: Run with Docker (Recommended)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/eventsphere-user-service.git
   cd eventsphere-user-service
   ```

2. **Start the service with Docker Compose:**
   ```bash
   docker-compose up --build
   ```

3. **Access the service:**
    - Service URL: `http://localhost:8081`
    - Health Check: `http://localhost:8081/actuator/health`

### Option 2: Local Development

1. **Start PostgreSQL:**
   ```bash
   docker run -d --name postgres-user-service \
     -e POSTGRES_DB=user_service_db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=password \
     -p 5432:5432 \
     postgres:15
   ```

2. **Run the application:**
   ```bash
   mvn clean spring-boot:run
   ```

### Option 3: With Full EventSphere Stack

1. **Clone infrastructure repository:**
   ```bash
   git clone https://github.com/yourusername/eventsphere-infrastructure.git
   cd eventsphere-infrastructure
   ```

2. **Start all services:**
   ```bash
   docker-compose up --build
   ```

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/user_service_db` | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` | Yes |
| `JWT_SECRET` | JWT signing secret | `defaultSecret` | Yes (Change in production) |
| `JWT_EXPIRATION` | Token expiration time (ms) | `86400000` (24h) | No |
| `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` | Eureka server URL | `http://localhost:8761/eureka/` | Yes |
| `SERVER_PORT` | Service port | `8081` | No |

### Application Profiles

#### application.yml (Default)
```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/user_service_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: ${JWT_SECRET:defaultSecretKeyThatShouldBeReplacedInProduction}
  expiration: ${JWT_EXPIRATION:86400000}

logging:
  level:
    com.eventsphere.user: DEBUG
```

#### application-docker.yml (Docker Environment)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/user_service_db

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
```

## API Usage Examples

### User Registration
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "USER"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "USER",
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### User Login
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phoneNumber": "+1234567890",
      "role": "USER",
      "createdAt": "2024-01-15T10:30:00"
    }
  },
  "timestamp": "2024-01-15T10:30:15"
}
```

### Get User Profile (Authenticated)
```bash
curl -X GET http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── eventsphere/
│   │           └── user/
│   │               ├── UserServiceApplication.java
│   │               ├── controller/
│   │               │   └── UserController.java
│   │               ├── service/
│   │               │   └── UserService.java
│   │               ├── repository/
│   │               │   └── UserRepository.java
│   │               ├── entity/
│   │               │   ├── User.java
│   │               │   └── Role.java
│   │               ├── dto/
│   │               │   ├── UserRegistrationRequest.java
│   │               │   ├── LoginRequest.java
│   │               │   ├── UserResponse.java
│   │               │   └── AuthenticationResponse.java
│   │               ├── mapper/
│   │               │   └── UserMapper.java
│   │               ├── config/
│   │               │   ├── SecurityConfig.java
│   │               │   └── JwtConfig.java
│   │               ├── security/
│   │               │   ├── JwtUtil.java
│   │               │   ├── JwtAuthenticationFilter.java
│   │               │   └── UserDetailsServiceImpl.java
│   │               └── exception/
│   │                   ├── GlobalExceptionHandler.java
│   │                   ├── UserNotFoundException.java
│   │                   └── DuplicateEmailException.java
│   └── resources/
│       ├── application.yml
│       ├── application-docker.yml
│       └── application-test.yml
└── test/
    └── java/
        └── com/
            └── eventsphere/
                └── user/
                    ├── controller/
                    ├── service/
                    └── repository/
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## Development Guidelines

### Code Style
- Follow Java naming conventions
- Use Lombok for reducing boilerplate
- Implement comprehensive logging
- Write meaningful commit messages

### Security Considerations
- Never log sensitive information (passwords, tokens)
- Use environment variables for secrets
- Implement rate limiting in production
- Regular dependency updates

### Performance
- Use connection pooling
- Implement caching where appropriate
- Add database indexes
- Monitor query performance

## Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check database logs
docker logs postgres-user-service

# Test connection
docker exec -it postgres-user-service psql -U postgres -d user_service_db
```

#### Service Discovery Issues
```bash
# Check Eureka server status
curl http://localhost:8761/eureka/apps

# Verify service registration
curl http://localhost:8761/eureka/apps/user-service
```

#### JWT Token Issues
- Verify JWT secret is set correctly
- Check token expiration time
- Ensure proper Authorization header format: `Bearer <token>`

## Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch from `develop`
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Code Review Checklist
- [ ] Code follows project conventions
- [ ] All tests pass
- [ ] New features have tests
- [ ] Documentation is updated
- [ ] No sensitive data in commits
- [ ] Logging is appropriate

## Monitoring & Observability

### Health Checks
- Service health: `GET /actuator/health`
- Database connectivity check
- Eureka registration status

### Metrics
- Request count and latency
- Database connection pool metrics
- Authentication success/failure rates
- JVM metrics

### Logging
- Structured logging with correlation IDs
- Request/response logging (excluding sensitive data)
- Error tracking and alerting

## Deployment

### Docker Deployment
```bash
# Build image
docker build -t eventsphere/user-service:latest .

# Run container
docker run -d \
  --name user-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/user_service_db \
  -e JWT_SECRET=your-production-secret \
  eventsphere/user-service:latest
```

### Production Checklist
- [ ] Change default JWT secret
- [ ] Set up proper database with backups
- [ ] Configure HTTPS/SSL
- [ ] Set up monitoring and alerting
- [ ] Configure log aggregation
- [ ] Set up CI/CD pipeline
- [ ] Implement rate limiting
- [ ] Set up load balancing

## Related Services

- **Discovery Service**: Service registration and discovery
- **Gateway Service**: API routing and load balancing
- **Event Service**: Event management functionality
- **Ticket Service**: Ticket booking and payment processing

## Support

### Documentation
- [EventSphere Architecture Guide](../docs/architecture.md)
- [API Documentation](http://localhost:8081/swagger-ui.html)
- [Development Setup Guide](../docs/development-setup.md)

### Contact
- **Project Lead**: [Your Name]
- **Team**: EventSphere Development Team
- **Issues**: [GitHub Issues](https://github.com/yourusername/eventsphere-user-service/issues)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Note**: This service is part of the EventSphere microservices platform. For complete setup instructions, refer to the [EventSphere Infrastructure Repository](https://github.com/yourusername/eventsphere-infrastructur