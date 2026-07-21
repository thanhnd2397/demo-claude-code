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

URLs: Web `http://localhost:8080` · API `http://localhost:8080/api/v1`

## Database Migrations

```powershell
./mvnw.cmd migration:up -P=local -N       # run migrations
./mvnw.cmd migration:status -P=local -N   # check status
./mvnw.cmd migration:new -Dmigration.description=<desc> -P=local -N  # new migration
```

Migrations run on primary only; replica replicates schema changes automatically.

Tailwind is compiled automatically during the Maven build.

## Profiles

| Profile | Use |
|---------|-----|
| `local` | Development (default) |
| `test` | Testing |
| `prod` | Production |

Config files: `web/src/main/resources/application.properties` · `application-local.properties` · `application-test.properties`
