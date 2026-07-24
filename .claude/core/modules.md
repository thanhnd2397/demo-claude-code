# Module Structure

## Modules

| Module | Location | Purpose | CLAUDE.md |
|--------|----------|---------|-----------|
| **web** | `web/` | Application entry point, main class, config files | — |
| **presentation** | `presentation/` | HTTP layer: controllers, filters, security, templates | `presentation/CLAUDE.md` |
| **application** | `application/` | Use cases, request/response DTOs, mappers | `application/CLAUDE.md` |
| **domain** | `domain/` | Domain models, adapter interfaces (ports), enums, exceptions | `domain/CLAUDE.md` |
| **infrastructure** | `infrastructure/` | Adapters, JPA entities, repositories, cache, DB migrations | `infrastructure/CLAUDE.md` |
| **util** | `util/` | Constants, helpers, response models, base exceptions | `util/CLAUDE.md` |

## Project Tree (abbreviated)

```
monolithic/
├── web/src/main/java/.../web/MonolithicApplication.java
├── presentation/src/main/java/.../presentation/{api,filter,handler,config}/
├── application/src/main/java/.../application/usecase/
├── domain/src/main/java/.../domain/{model,adapter,enums,exception}/
├── infrastructure/src/main/
│   ├── java/.../infrastructure/{adapter,persistence,cache,exception}/
│   └── resources/db/migration/
├── util/src/main/java/.../util/{constant,helper,response,exception}/
├── docker/{docker-compose.yml,app/,mysql-primary/,mysql-replica/,redis/,nginx/}
└── pom.xml
```
