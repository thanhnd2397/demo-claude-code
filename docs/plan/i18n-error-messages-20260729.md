# Plan: i18n error messages (English + Vietnamese) resolved by Accept-Language

## Document metadata

| Field | Value |
|-------|-------|
| Created date | 2026/07/29 |
| Created by | Claude Code |
| Approved date | 2026/07/29 |
| Approved by | thanhnd |
| Spec ref | Adhoc — no spec doc |
| Version | 1.0 |

## Context

Every error the API returns today carries a code like `E-01-ADMINISTRATOR-0003` as **both** `error_code` and `error_message` in the JSON response — there is no human-readable text, and no translation step anywhere. The domain layer was actually already designed for i18n: `DomainError.errorCode` is documented as an "i18n key" and already carries an `args` array that nothing consumes yet, and `.claude/core/modules/presentation.md` describes an aspirational `ApiExceptionHandler` that "uses MessageSource for i18n error messages" — never implemented.

User wants: a `messages.properties` (English) and `messages_vi.properties` (Vietnamese) pair, with the locale picked from the incoming HTTP request (`Accept-Language` header), and the existing error codes actually translated through them instead of the current code-as-message duplication.

## Approach

### 1. Message bundles

New files in `web/src/main/resources/` (the module that produces the runnable jar — Spring Boot's `MessageSourceAutoConfiguration` auto-discovers `classpath:messages*.properties` there with zero extra config, since default `spring.messages.basename=messages` already matches):

- `messages.properties` — English, one key per existing error code (from `docs/architecture/project-overview.md` §6): `E-01-ADMINISTRATOR-0001..0011,0013`, `E-03-REDIS-0001..0005`, `E-03-MAIL-0001,0002`, `E-00-CORE-0001`.
- `messages_vi.properties` — same keys, Vietnamese text.

Add to `web/src/main/resources/application.properties`: `spring.messages.encoding=UTF-8` (Vietnamese diacritics) and `spring.messages.fallback-to-system-locale=false` (never silently fall back to the JVM's default locale — always English unless `Accept-Language` says otherwise).

### 2. Locale resolution from the HTTP request

New file: `presentation/src/main/java/vn/thanhnd/demo/presentation/config/LocaleConfiguration.java` — a `LocaleResolver` bean named `localeResolver` (`AcceptHeaderLocaleResolver`, default English, supported [en, vi]). Reads `Accept-Language` per request, no session/cookie state — matches a stateless JWT API. Spring's `DispatcherServlet` looks this bean up by exact name and sets `LocaleContextHolder` before invoking any controller/`@RestControllerAdvice`.

### 3. Actually resolve messages instead of duplicating the code

Two different code paths need two different techniques, because Spring Security's filter chain runs **before** `DispatcherServlet` — `LocaleContextHolder` is only populated automatically once the request reaches the servlet.

**A. Inside normal MVC dispatch** (`LocaleContextHolder.getLocale()` already correct — just inject `MessageSource`):
- `ApiExceptionHandler` — both handlers resolve `messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale())` (code itself as default, so an untranslated code never breaks the response).
- `BaseController` — add `MessageSource` alongside `ResponseMaker`; in `toResponseEntity`, resolve `messageSource.getMessage(error.errorCode(), error.args(), error.errorCode(), LocaleContextHolder.getLocale())` — finally wires up `DomainError.args()`.
- `AuthController` / `AdministratorController` — thread the new `MessageSource` constructor param through to `super(...)`.

**B. Before MVC dispatch** (security filter chain — must resolve locale manually from the request):
- `JwtAuthenticationFilter` (invalid/blacklisted token → writes 401 directly)
- `RestAuthenticationEntryPoint` (401 — no `Authorization` header)
- `RestAccessDeniedHandler` (403 — missing role/authority)

Each gets `LocaleResolver` + `MessageSource` injected, resolves `Locale locale = localeResolver.resolveLocale(request);` then looks up the message — otherwise the most commonly hit failures (missing/expired token, wrong permission) stay untranslated while everything else is fixed.

### Explicitly out of scope

- Localizing Bean Validation's default messages (`@NotBlank` etc.) — routed through a `MethodArgumentNotValidException` path `ApiExceptionHandler` doesn't handle at all today; pre-existing, separate gap.
- Adding new error codes or changing any use case's validation logic.

## Files touched

| Type | File |
| --- | --- |
| New | `web/src/main/resources/messages.properties` |
| New | `web/src/main/resources/messages_vi.properties` |
| New | `presentation/.../config/LocaleConfiguration.java` |
| Edit | `web/src/main/resources/application.properties` |
| Edit | `presentation/.../handler/ApiExceptionHandler.java` |
| Edit | `presentation/.../api/BaseController.java` |
| Edit | `presentation/.../api/AuthController.java` |
| Edit | `presentation/.../api/AdministratorController.java` |
| Edit | `presentation/.../filter/JwtAuthenticationFilter.java` |
| Edit | `presentation/.../handler/RestAuthenticationEntryPoint.java` |
| Edit | `presentation/.../handler/RestAccessDeniedHandler.java` |

## Verify

```bash
./mvnw clean compile -P=local
```
End-to-end (via `docker compose up`): login with wrong password + `Accept-Language: vi` → Vietnamese `error_message`; same with `en` → English; no-Authorization-header protected call + `Accept-Language: vi` → Vietnamese 401 body; forbidden-role call + `Accept-Language: vi` → Vietnamese 403 body.
