# Development Setup

## Prerequisites

- Java 25 (any vendor JDK distribution)
- Maven 3.6+
- Docker & Docker Compose

JAVA_HOME is machine-specific — set it in `CLAUDE.local.md`, not here.

## Build

```powershell
# Build all modules
./mvnw.cmd clean install

# With profile
./mvnw.cmd clean install -P=local
```

## Run

```powershell
# Docker Compose (recommended — starts MySQL, Redis, Nginx, App)
cd docker && docker-compose up -d

# Locally
cd web && ../mvnw.cmd spring-boot:run
```

URL: API `http://localhost:8080/api/v1`

## Database Migrations

Migrations run automatically on application startup (Spring Boot's Flyway auto-configuration). To run them standalone without booting the app:

```powershell
./mvnw.cmd flyway:info -pl infrastructure       # check status
./mvnw.cmd flyway:migrate -pl infrastructure    # run migrations
```

New migration files are added by hand at `infrastructure/src/main/resources/db/migration/V{next}__{description}.sql` (Flyway naming convention, sequential version numbers).

Migrations run on primary only; replica replicates schema changes automatically.

## Profiles

| Profile | Use |
|---------|-----|
| `local` | Development (default) |
| `test` | Testing |
| `prod` | Production |

Config files: `web/src/main/resources/application.properties` · `application-local.properties` · `application-test.properties`
