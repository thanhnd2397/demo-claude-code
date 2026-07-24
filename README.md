# thanhnd-demo-claude

Spring Boot Clean Architecture demo skeleton.

Java 25 · Spring Boot 4.0.1 · MySQL (primary/replica) + Redis · REST API

## Architecture

Clean Architecture with dependencies pointing inward toward the domain layer:

| Module | Location | Purpose |
| ------ | -------- | ------- |
| web | `web/` | Application entry point |
| presentation | `presentation/` | HTTP layer — controllers, filters, security |
| application | `application/` | Use cases, DTOs |
| domain | `domain/` | Business logic, domain models, adapter interfaces (ports) |
| infrastructure | `infrastructure/` | Adapters, JPA entities, repositories, DB migrations |
| util | `util/` | Constants, helpers, response models, base exceptions |

See `.claude/core/architecture.md` and `.claude/core/modules.md` for full details.

## Prerequisites

- JDK 25
- Maven (or use the bundled `./mvnw` / `mvnw.cmd` wrapper)
- Docker + Docker Compose (for MySQL primary/replica and Redis)

## Running locally

1. Start the infrastructure services:

   ```bash
   docker compose -f docker/docker-compose.yml up -d mysql-primary mysql-replica redis
   ```

2. Build the project:

   ```bash
   ./mvnw clean compile -P=local
   ```

3. (Optional) Run database migrations standalone, without starting the app:

   ```bash
   ./mvnw -pl infrastructure flyway:migrate -P=local
   ```

   Otherwise, migrations run automatically when the app starts (step 4), via Spring Boot's Flyway auto-configuration.

4. Start the application:

   ```bash
   ./mvnw spring-boot:run -pl web -P=local -Dspring-boot.run.profiles=local
   ```

   The API starts on `http://localhost:8080/api/v1`.

### Full stack via Docker Compose

To run the entire stack (app, MySQL primary/replica, Redis, Nginx) in containers:

```bash
docker compose -f docker/docker-compose.yml up -d
```

## Database Migrations

Migrations are plain SQL files under `infrastructure/src/main/resources/db/migration/`, managed by Flyway.

### Creating a new migration

1. Add a new file directly in `infrastructure/src/main/resources/db/migration/`, named:

   ```text
   V{next}__{description}.sql
   ```

   - `{next}` is the next sequential version number (e.g. if `V1__create_auth_tables.sql` exists, the next file is `V2__...`).
   - `{description}` is lowercase, underscore-separated (e.g. `add_email_index`).
   - Example: `V2__add_administrator_last_login_at.sql`.

2. Write plain SQL in the file — no special markers or undo section (Flyway Community does not run undo/down migrations; write a new forward migration to revert a mistake instead).

### Running migrations

Migrations run **automatically** every time the app starts (`spring-boot:run`), via Spring Boot's Flyway auto-configuration — no manual step is required in normal development.

To run them standalone, without starting the app:

```bash
./mvnw -pl infrastructure flyway:info -P=local      # check status of all migrations
./mvnw -pl infrastructure flyway:migrate -P=local   # apply pending migrations
```

`flyway:clean` (drops all objects in the schema) is disabled by default for safety. Only use it against a local, disposable database, and only with the override flag:

```bash
./mvnw -pl infrastructure flyway:clean -Dflyway.cleanDisabled=false -P=local
```

## Configuration

Local datasource, Redis, and mail settings live in `web/src/main/resources/application-local.properties`. Machine-specific overrides (e.g. `JAVA_HOME`) go in `CLAUDE.local.md` (not checked in).

## Conventions

Project conventions, layer rules, and coding guardrails are documented in `CLAUDE.md` and `.claude/rules/`.
