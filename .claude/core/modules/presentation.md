# Presentation Layer — Detailed Reference

## Exception Handlers

### ApiExceptionHandler

```java
@RestControllerAdvice(basePackages = "vn.thanhnd.demo.presentation.api")
public class ApiExceptionHandler {
    // Returns ResponseEntity<RestResponse<T>> with appropriate HTTP status
    // Uses MessageSource for i18n error messages
}
```

---

## Filter Details

### JwtAuthenticationFilter

- Processes only `/api/**` requests
- Extracts Bearer token from `Authorization` header
- Validates JWT, loads authorities via `GetAuthorityUseCase`
- Sets `SecurityContextHolder` with user authorities

### ApiLoginFilter (`POST /api/v1/auth/login`)

- Extends `AbstractAuthenticationProcessingFilter`
- On success: generates JWT + refresh token, updates login info, returns JSON
- On failure: returns 401 JSON response

---

## Security Configuration Details

### API Filter Chain

- Stateless (`SessionCreationPolicy.STATELESS`)
- JWT via `JwtAuthenticationFilter`
- CSRF disabled
- Security headers: `X-Frame-Options: DENY`, `nosniff`, `HSTS`, CSP
- Public: `/api/v1/auth/login`, `/api/v1/auth/refresh-token`

---

## Transaction Routing (DatabaseConfiguration)

`@Transactional(readOnly = true)` → `TransactionSynchronizationManager.isCurrentTransactionReadOnly()` → replica.
`@Transactional` → primary.

Multiple replicas use round-robin. If all replicas are unhealthy, falls back to primary.
`ReplicaHealthChecker` periodically validates replicas; failed replicas enter cooldown.

Key properties:

- `datasource.replica.health-check-interval-ms`
- `datasource.replica.failover-cooldown-seconds`
- `datasource.replica.max-consecutive-failures`

---

## Async Configuration (AsyncConfiguration)

- Virtual threads enabled via `spring.threads.virtual.enabled=true`
- Automatic fallback to traditional thread pool if virtual threads fail
- Tomcat configured to use virtual threads for request handling
- Task decorator captures tracing + security context for async operations
- Metrics via Micrometer; health indicator included
