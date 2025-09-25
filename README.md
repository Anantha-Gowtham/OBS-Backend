# OBS Banking System - Backend

A secure, modern banking system backend built with Spring Boot.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- MySQL 8.0+

### Environment Setup

1. **Clone the repository**
```bash
git clone https://github.com/Anantha-Gowtham/OBS-Backend.git
cd OBS-Backend
```

2. **Create environment file**
```bash
cp .env.example .env
```

3. **Configure your environment variables**
```env
# Database
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=obs_banking

# Email (SMTP)
MAIL_USERNAME=your_email@domain.com  
MAIL_PASSWORD=your_app_password

# JWT Security
JWT_SECRET=your_secure_jwt_secret_32_chars_min
JWT_EXPIRATION=86400000
```

### Run with Docker

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f obs-backend
```

The backend will be available at `http://localhost:8080`
- Security configuration (stateless, CORS, open auth endpoints)
- Email service for OTP & alerts (async)
- Health endpoint: `/api/health`

## TODO (Next Steps)
- Proper JWT implementation (replace placeholder token)
- Role-based method security & authorities
- Account / Transaction services & controllers
- Branch & Employee management endpoints
- Transaction OTP enforcement
- Audit logging
- Unit & integration tests

## Running Locally
1. Ensure MySQL is running and credentials match `application.yml`.
2. Create database (or let Hibernate auto-create):
```sql
CREATE DATABASE IF NOT EXISTS obs_banking_system;
```
3. Build & run:
```bash
mvn spring-boot:run
```
Backend will start on: `http://localhost:8085/api`

## Swagger / API Docs
After adding more controllers, docs will be available at:
- OpenAPI JSON: `/api/api-docs`
- Swagger UI: `/api/swagger-ui.html`

## Security Notes
- Replace `jwt.secret` with a strong secret in production.
- Never commit real SMTP app passwords; move them to environment variables.
- Current token generation is a placeholder — implement real JWT ASAP.
