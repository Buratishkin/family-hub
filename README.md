# FamilyHub

FamilyHub is a Java 21 / Spring Boot backend for household collaboration: families, members, tasks, addresses, meal planning, polls, schedules, authentication, and notifications.

The project is structured as a small backend-for-frontend application plus extracted domain services behind a lightweight HTTP gateway.

## Stack

- Java 21
- Spring Boot 4
- Maven
- PostgreSQL
- Kafka
- Spring Security with JWT access and refresh tokens
- Spring Data JPA
- Firebase Cloud Messaging support for push notifications

## Services

- `gateway`: the public backend entrypoint. It routes frontend HTTP requests to the service that owns the requested domain.
- Root application: a BFF layer for `/me/**` read models, health checks, and legacy internal adapters.
- `auth-service`: registration, login, logout, refresh tokens, password hashing, JWT issuing, and user registration events.
- `family-service`: families, members, roles, invites, aliases, family plans, polls, and internal family access checks.
- `address-service`: family addresses, address categories, map URL helpers, and address/category domain events.
- `task-service`: task creation and updates, assignees, statuses, recurrence, reminders, old/current task views, and schedule conflict checks.
- `meal-service`: food ingredients, recipes, daily meal plans, ingredient summaries, and shopping-task creation through task-service.
- `notification-service`: in-app notifications, device registrations, push delivery attempts, user/member projections, and Firebase/logging push senders.

## Architecture Notes

- Frontend clients should use the gateway on `http://localhost:8082`.
- Each extracted service owns its database schema.
- Cross-service read dependencies use internal HTTP endpoints or Kafka-backed local projections.
- Domain events are persisted through a transactional outbox and published to Kafka.
- Consumers use an inbox/idempotency table to process events safely.
- Local development uses Docker Compose for PostgreSQL databases and Kafka.

## Local Configuration

Runtime secrets are loaded from environment variables. Use `.env.example` as the local template and keep the real `.env` file untracked.

Required variables:

- `FAMILYHUB_POSTGRES_USER`
- `FAMILYHUB_POSTGRES_PASSWORD`
- `FAMILYHUB_JWT_SECRET`
- `FAMILYHUB_ENCRYPTOR_PASSWORD`
- `FAMILYHUB_ENCRYPTOR_SALT`
- `FAMILYHUB_INTERNAL_TOKEN`

For local development, notification push defaults to logging. Set `FAMILYHUB_NOTIFICATION_PUSH_PROVIDER=firebase` and `FIREBASE_CREDENTIALS_PATH` only when testing real Firebase Cloud Messaging delivery.

## Run Locally

```powershell
Copy-Item .env.example .env
.\scripts\dev-services.ps1 -Action restart
```

Gateway:

```text
http://localhost:8082
```

Health checks:

```text
GET http://localhost:8082/gateway/health
GET http://localhost:8080/health
GET http://localhost:8081/health
GET http://localhost:8083/health
GET http://localhost:8084/health
GET http://localhost:8085/health
GET http://localhost:8086/health
GET http://localhost:8087/health
```

## Verify

```powershell
.\mvnw.cmd test
.\mvnw.cmd -f auth-service\pom.xml test
.\mvnw.cmd -f family-service\pom.xml test
.\mvnw.cmd -f address-service\pom.xml test
.\mvnw.cmd -f task-service\pom.xml test
.\mvnw.cmd -f meal-service\pom.xml test
.\mvnw.cmd -f notification-service\pom.xml test
.\mvnw.cmd -f gateway\pom.xml test
```

End-to-end smoke checks:

```powershell
.\scripts\smoke-auth-service.ps1
.\scripts\smoke-full-stack.ps1
```

