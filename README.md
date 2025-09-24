# OBS Backend

Spring Boot backend for the Online Banking System (OBS).

## Modules Implemented
- User registration & login with OTP verification
- Account lockout after repeated failed attempts (email alerts)
- Password reset via email token
- Basic domain models: User, Account, Branch, Employee, Transaction, OTP, PasswordResetToken
- Repositories for persistence (Spring Data JPA)
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
