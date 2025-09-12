# EventSphere User Service

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📋 Overview

The **EventSphere User Service** is a comprehensive microservice responsible for user management, authentication, and authorization within the EventSphere platform. It provides secure user registration, JWT-based authentication, role-based access control, and extensive admin functionality for user management.

## ✨ Features

### 🔐 Authentication & Authorization
- **User Registration** with email verification
- **Secure Login** with JWT access tokens and refresh tokens
- **Role-based Access Control** (USER, ORGANIZER, ADMIN)
- **Password Management** (change, reset via email)
- **Token Refresh** mechanism for seamless user experience
- **Secure Logout** with token invalidation

### 👤 User Management
- **User Profile Management** (create, read, update, delete)
- **Email Verification** system
- **Account Status Management** (activate/deactivate)
- **User Search** and filtering capabilities
- **Login Attempt Tracking** for security monitoring

### 👨‍💼 Admin Features
- **Complete User Administration** with role management
- **User Role Assignment** and removal
- **Advanced User Search** by name, email, or criteria
- **User Analytics** (registration date ranges, activity tracking)
- **Bulk Operations** (purge inactive/unverified users)
- **Email Management** (resend verification, update emails)
- **Security Monitoring** (view login attempts per user)

### 🛡️ Security Features
- **JWT Authentication** with RS256 algorithm
- **Password Encryption** using BCrypt
- **Input Validation** with Bean Validation
- **SQL Injection Prevention** with JPA
- **CORS Configuration** for cross-origin requests
- **Method-level Security** with @PreAuthorize annotations
- **Global Exception Handling** for security errors

### 📊 Additional Features
- **Comprehensive Logging** for audit trails
- **Health Check Endpoints** for monitoring
- **Email Service Integration** for notifications
- **Docker Support** for containerization
- **Database Migration** scripts included
- **API Testing Suite** included

## 🏗️ Architecture

### Tech Stack
- **Framework**: Spring Boot 3.5.5
- **Security**: Spring Security 6 + JWT
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Bean Validation (Jakarta Validation)
- **Email**: Spring Mail with Gmail SMTP
- **Build Tool**: Maven 3.9+
- **Java Version**: 21
- **Containerization**: Docker & Docker Compose

### Project Structure
```
src/main/java/org/com/eventsphere/user/
├── Auth/                    # JWT Authentication filters
├── config/                  # Configuration classes
├── controller/              # REST API controllers
├── dto/                     # Data Transfer Objects
├── entity/                  # JPA entities
├── exception/               # Custom exceptions & handlers
├── mapper/                  # Entity-DTO mappers
├── repository/              # Data access layer
├── security/                # Security utilities
├── service/                 # Business logic layer
└── utils/                   # Utility classes
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 15+
- Docker (optional)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd eventsphere-user-service
```

### 2. Database Setup

#### Option A: Using Docker (Recommended)
```bash
# Start PostgreSQL with Docker Compose
docker-compose up -d

# Or use the provided script
./scripts/start-db.sh
```

#### Option B: Manual PostgreSQL Setup
```bash
# Create database
createdb eventsphere_user_db

# Run initialization script
psql -d eventsphere_user_db -f scripts/init-user-db.sql
```

### 3. Configure Application
Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eventsphere_user_db
    username: your_username
    password: your_password
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000  # 24 hours
```

### 4. Run the Application
```bash
# Using Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/eventsphere-user-service-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8081`

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/register` | Register new user | No |
| POST | `/api/v1/auth/login` | User login | No |
| POST | `/api/v1/auth/refresh-token` | Refresh JWT token | No |
| GET | `/api/v1/auth/verify-email` | Verify email address | No |
| POST | `/api/v1/auth/forgot-password` | Request password reset | No |
| POST | `/api/v1/auth/reset-password` | Reset password with token | No |
| POST | `/api/v1/auth/logout` | User logout | No |

### User Management Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/users/profile` | Get current user profile | Yes |
| GET | `/api/v1/users/{id}` | Get user by ID | Yes |
| PUT | `/api/v1/users/update/{id}` | Update user profile | Yes (Self/Admin) |
| PUT | `/api/v1/users/change-password` | Change password | Yes |
| DELETE | `/api/v1/users/delete/{id}` | Delete user account | Yes (Self/Admin) |

### Admin Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/users/all` | Get all users | Admin |
| POST | `/api/v1/users/assign-role` | Assign role to user | Admin |
| POST | `/api/v1/users/remove-role` | Remove role from user | Admin |
| GET | `/api/v1/users/by-role/{role}` | Get users by role | Admin |
| PUT | `/api/v1/users/deactivate/{id}` | Deactivate user | Admin |
| PUT | `/api/v1/users/reactivate/{id}` | Reactivate user | Admin |
| GET | `/api/v1/users/search` | Search users | Admin |
| GET | `/api/v1/users/inactive` | Get inactive users | Admin |
| GET | `/api/v1/users/registered-between` | Get users by date range | Admin |
| GET | `/api/v1/users/last-active-before` | Get users by last activity | Admin |
| DELETE | `/api/v1/users/purge/inactive` | Purge inactive users | Admin |
| DELETE | `/api/v1/users/purge/unverified` | Purge unverified users | Admin |
| POST | `/api/v1/users/resend-verification` | Resend verification email | Admin |
| PUT | `/api/v1/users/update-email/{id}` | Update user email | Admin |
| GET | `/api/v1/users/login-attempts/{email}` | Get login attempts | Admin |

## 🧪 Testing

### Automated Testing
```bash
# Run the comprehensive API test suite
./test-api.sh
```

### Manual Testing Examples

#### Register a User
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "phone": "+1234567890"
  }'
```

#### User Login
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }'
```

#### Get User Profile (with JWT)
```bash
curl -X GET http://localhost:8081/api/v1/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Admin: Get All Users
```bash
curl -X GET http://localhost:8081/api/v1/users/all \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## 🔧 Configuration

### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eventsphere_user_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# JWT Configuration
JWT_SECRET=your_secret_key_here
JWT_EXPIRATION=86400000

# Email Configuration
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password

# Server Configuration
SERVER_PORT=8081
```

### Email Setup
Follow the [Gmail Setup Guide](GMAIL_SETUP_GUIDE.md) for configuring email services.

## 🐳 Docker Deployment

### Using Docker Compose
```bash
# Start all services (app + database)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Building Docker Image
```bash
# Build the application image
docker build -t eventsphere-user-service .

# Run with external database
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/eventsphere_user_db \
  eventsphere-user-service
```

## 🔒 Security Considerations

### JWT Tokens
- **Access Tokens**: Short-lived (24 hours) for API authentication
- **Refresh Tokens**: Long-lived for obtaining new access tokens
- **Token Storage**: Tokens should be stored securely on the client side
- **Token Invalidation**: Logout invalidates refresh tokens

### Password Security
- Passwords are hashed using BCrypt with salt
- Minimum password requirements enforced
- Password reset tokens expire in 15 minutes

### API Security
- All admin endpoints require ADMIN role
- User-specific operations require ownership or admin privileges
- Input validation prevents injection attacks
- CORS configured for authorized origins only

## 📊 Monitoring & Health Checks

### Health Check Endpoints
- **Application Health**: `GET /actuator/health`
- **Database Status**: `GET /actuator/health/db`
- **Application Info**: `GET /actuator/info`

### Logging
- All authentication attempts are logged
- Admin operations are logged for audit trails
- Error logging with stack traces for debugging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the [HELP.md](HELP.md) file
- **Email Setup**: See [GMAIL_SETUP_GUIDE.md](GMAIL_SETUP_GUIDE.md)
- **Issues**: Create an issue in the repository
- **Discussions**: Use GitHub Discussions for questions

## 🚧 Roadmap

- [ ] OAuth2 integration (Google, Facebook, GitHub)
- [ ] Two-factor authentication (2FA)
- [ ] User activity analytics dashboard
- [ ] Rate limiting for API endpoints
- [ ] Real-time notifications
- [ ] API versioning support
- [ ] Swagger/OpenAPI documentation
- [ ] Comprehensive unit and integration tests

---

**EventSphere User Service** - Secure, scalable, and feature-rich user management for modern applications.
