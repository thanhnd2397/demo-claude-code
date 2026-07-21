# Technology Stack

## Backend

- **Java 25** — programming language
- **Spring Boot 4.0.1** — application framework
- **Spring Security** — authentication and authorization
- **Spring Data JPA / Hibernate** — data persistence
- **MyBatis Migrations** — database migration management

## Database & Caching

- **MySQL** — primary database with replica setup for read scaling
- **Redis** — caching and session storage
- **HikariCP** — connection pooling

## Frontend

- **Thymeleaf** — server-side template engine
- **Tailwind CSS** — utility-first CSS framework
- **HTMX** — dynamic web interactions without full page reloads

## Infrastructure

- **Docker / Docker Compose** — containerization and orchestration
- **Nginx** — reverse proxy and load balancing

## Build & Development

- **Maven** — build automation
- **Lombok** — boilerplate reduction (not used on domain models — those stay plain immutable records)
- **MapStruct** — main mapper framework between layers
- **Log4j2** — logging framework

## Testing

- **JUnit 5** — unit testing
- **Testcontainers** — integration testing with Docker containers
- **Spring Boot Test** — slice tests
- **MyBatis Migrations** — migration execution in tests
- **JaCoCo** — code coverage (100% branch coverage required)
