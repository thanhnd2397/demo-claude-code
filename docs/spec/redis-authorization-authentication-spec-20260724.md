# Spec: Redis-backed Authorization & Login Lockout

## Document metadata

| Field | Value |
|-------|-------|
| Created date | 2026/07/24 09:00 |
| Created by | Claude Code |
| Approved date | |
| Approved by | |
| Spec ref | `docs/plan/redis-authorization-authentication-20260724.md` |
| Version | 1.0 |

## Revision history

| Version | Date | Author | Summary of changes |
|---------|------|--------|--------------------|
| 1.0 | 2026/07/24 | Claude Code | Initial draft, derived from the approved plan |

---

## 1. New domain types

### 1.1 `AdministratorPermissionsCacheData`

File: `domain/src/main/java/vn/thanhnd/demo/domain/model/AdministratorPermissionsCacheData.java`

```java
@JsonTypeName("administratorPermissionsCacheData")
public record AdministratorPermissionsCacheData(Set<String> roleNames, Set<String> permissionNames)
        implements CacheData {
}
```

- Implements `vn.thanhnd.demo.domain.cache.CacheData`.
- Stored under cache key `CACHE_ADMINISTRATOR_PERMISSIONS_{administratorId}` (see §5).

### 1.2 `LoginFailureCacheData`

File: `domain/src/main/java/vn/thanhnd/demo/domain/model/LoginFailureCacheData.java`

```java
@JsonTypeName("loginFailureCacheData")
public record LoginFailureCacheData(int attemptCount) implements CacheData {
}
```

- Implements `vn.thanhnd.demo.domain.cache.CacheData`.
- Stored under cache key `CACHE_ADMINISTRATOR_LOGIN_FAILURES_{username}` (see §5).

---

## 2. Cache key constants

File: `util/src/main/java/vn/thanhnd/demo/util/constant/ApplicationConstants.java`

Add two static helper methods, following the exact naming pattern of the two existing helpers (`cacheKeyAdministratorRefreshToken`, `cacheKeyAdministratorTokenBlacklist`):

```java
public static String cacheKeyAdministratorPermissions(String administratorId) {
    return "CACHE_ADMINISTRATOR_PERMISSIONS_" + administratorId;
}

public static String cacheKeyAdministratorLoginFailures(String username) {
    return "CACHE_ADMINISTRATOR_LOGIN_FAILURES_" + username;
}
```

---

## 3. Domain port change

File: `domain/src/main/java/vn/thanhnd/demo/domain/adapter/AdministratorRepositoryPort.java`

Add one method to the interface:

```java
/**
 * Fetch the administrator's current role and permission names, cache-aside via Redis.
 *
 * @param administratorId The administrator's id
 * @return The current role and permission names
 * @throws vn.thanhnd.demo.domain.exception.DomainValidationException (E-01-ADMINISTRATOR-0012) if the administrator does not exist
 */
AdministratorPermissionsCacheData findPermissions(String administratorId);
```

---

## 4. Infrastructure implementation

File: `infrastructure/src/main/java/vn/thanhnd/demo/infrastructure/adapter/AdministratorRepositoryAdapterImpl.java`

### 4.1 New dependency

Inject `CacheAdapter` (constructor field, `@RequiredArgsConstructor` already generates the constructor — no manual constructor edit needed).

### 4.2 `findPermissions(String administratorId)` — exact steps

1. Build `key = ApplicationConstants.cacheKeyAdministratorPermissions(administratorId)`.
2. `cacheAdapter.get(key, AdministratorPermissionsCacheData.class)` — if non-null, return it immediately (cache hit).
3. Cache miss: call `findEntityOrThrow(administratorId)` (existing private helper — throws `E-01-ADMINISTRATOR-0010` if not found; **do not introduce `E-01-ADMINISTRATOR-0012` here** — reuse `0010` since `findEntityOrThrow` already exists and throwing a second not-found code for the same lookup would be inconsistent. `E-01-ADMINISTRATOR-0012` is reserved in §6 for documentation clarity only if a reviewer wants a distinct code later; default implementation reuses `0010`).
4. Map the entity: `Administrator administrator = administratorMapper.toDomain(entity)`.
5. Build `AdministratorPermissionsCacheData data = new AdministratorPermissionsCacheData(administrator.roleNames(), administrator.permissionNames())`.
6. `cacheAdapter.set(key, data, LocalDateTime.now().plusMinutes(15))` — TTL is exactly 15 minutes.
7. Return `data`.

### 4.3 Cache invalidation

In `updateRoles(String administratorId, Set<String> roleNames)`, after the existing `administratorJpaRepository.save(entity)` call and before `return`:
```java
cacheAdapter.delete(ApplicationConstants.cacheKeyAdministratorPermissions(administratorId));
```

In `updateStatus(String administratorId, AdministratorStatus status)`, same addition after `administratorJpaRepository.save(entity)` and before `return`.

No NFR notes beyond this — invalidation happens synchronously in the same transaction as the write.

---

## 5. Application layer changes

### 5.1 `ValidateAccessTokenUseCase`

File: `application/src/main/java/vn/thanhnd/demo/application/usecase/administrator/ValidateAccessTokenUseCase.java`

Add `AdministratorRepositoryPort` as a constructor dependency.

Updated `validate(String accessToken)` — orchestration steps (for the JavaDoc, matching the project's use-case comment template):
1. Parse the access token and verify its signature and expiry (`tokenProvider.parse`) — unchanged.
2. Reject the token if its `jti` is blacklisted in Redis (`E-01-ADMINISTRATOR-0006`) — unchanged.
3. **New**: fetch the administrator's current role and permission names via `administratorRepositoryPort.findPermissions(claims.administratorId())`.
4. Return `AccessTokenClaims(claims.administratorId(), live.roleNames(), live.permissionNames())` — note: `AccessTokenClaims` itself is unchanged; only the source of `roleNames()`/`permissionNames()` changes from `claims` (JWT) to `live` (cache/DB).

### 5.2 `LoginUseCase`

File: `application/src/main/java/vn/thanhnd/demo/application/usecase/administrator/LoginUseCase.java`

Add two `private static final` constants:
```java
private static final int MAX_FAILED_ATTEMPTS = 5;
private static final long LOCKOUT_WINDOW_MINUTES = 15;
```

Updated `login(LoginRequest request)` — orchestration steps:
1. **New**: build `key = ApplicationConstants.cacheKeyAdministratorLoginFailures(request.username())`; read `LoginFailureCacheData failures = cacheAdapter.get(key, LoginFailureCacheData.class)`; if `failures != null && failures.attemptCount() >= MAX_FAILED_ATTEMPTS`, throw `DomainValidationException("E-01-ADMINISTRATOR-0013")`.
2. Find administrator by username and verify the password against the stored BCrypt hash — unchanged.
3. **New**: on password mismatch (the existing `E-01-ADMINISTRATOR-0003` throw path), before throwing: compute `int nextCount = (failures == null ? 0 : failures.attemptCount()) + 1`; `cacheAdapter.set(key, new LoginFailureCacheData(nextCount), LocalDateTime.now().plusMinutes(LOCKOUT_WINDOW_MINUTES))`.
4. Validate the account status is ACTIVE (`E-01-ADMINISTRATOR-0004`) — unchanged.
5. **New**: on reaching this point (successful authentication), `cacheAdapter.delete(key)` to reset the failure counter.
6. Issue access token and refresh token, store refresh token in Redis, return `LoginResponse` — unchanged.

---

## 6. Error codes

| Code | Meaning | HTTP mapping | Error message (Vietnamese) | Error message (English) |
|------|---------|--------------|------------------------------|--------------------------|
| `E-01-ADMINISTRATOR-0012` | Reserved: administrator not found during token validation (distinct from `0010` if a reviewer later wants to split the not-found reason for token-validation vs. direct lookup; **default implementation in §4.2 reuses `0010`, this code is not thrown unless that split is explicitly requested**) | 401 (via `JwtAuthenticationFilter`, regardless of code) | "Không tìm thấy tài khoản quản trị." | "Administrator account not found." |
| `E-01-ADMINISTRATOR-0013` | Account temporarily locked after `MAX_FAILED_ATTEMPTS` (5) consecutive failed login attempts within `LOCKOUT_WINDOW_MINUTES` (15) | 400 (via `AuthController`, same as all other login errors) | "Tài khoản tạm thời bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau." | "Account temporarily locked due to too many failed login attempts. Please try again later." |

Note: the current runtime (`AuthController.toResponseEntity`, `AdministratorController.toResponseEntity`) passes `error.errorCode()` as both the error code and the error message — no i18n message resolution is wired up yet anywhere in this project. The bilingual messages above are the intended user-facing text for whenever message resolution is added; they are not yet rendered by the running API.

---

## 7. Effect on existing endpoints

No endpoint's method, path, or request/response shape changes. Two existing endpoints gain a new error case or an added internal step:

### `POST /api/v1/auth/login`
- **New error case**: `400 Bad Request`, code `E-01-ADMINISTRATOR-0013`, when the account has reached `MAX_FAILED_ATTEMPTS` (5) consecutive failed attempts within the last `LOCKOUT_WINDOW_MINUTES` (15) minutes — checked before password verification.

### `PATCH /api/v1/administrators/{id}/roles`
- **New step**: after persisting the role change, invalidate `CACHE_ADMINISTRATOR_PERMISSIONS_{id}` in Redis (§4.3).

### `PATCH /api/v1/administrators/{id}/status`
- **New step**: after persisting the status change, invalidate `CACHE_ADMINISTRATOR_PERMISSIONS_{id}` in Redis (§4.3).

All other endpoints (`/auth/register`, `/auth/refresh`, `/auth/logout`, `GET /administrators`) are unaffected.

---

## 8. Cache key summary

| Key pattern | Data | TTL | Set by | Invalidated by |
|---|---|---|---|---|
| `CACHE_ADMINISTRATOR_REFRESH_TOKEN_{administratorId}` | `RefreshTokenCacheData` (existing) | Refresh token lifetime | `LoginUseCase`, `RefreshTokenUseCase` | `LogoutUseCase`, token rotation |
| `CACHE_ADMINISTRATOR_TOKEN_BLACKLIST_{jti}` | `TokenBlacklistCacheData` (existing) | Until access token's natural expiry | `LogoutUseCase` | N/A (expires naturally) |
| `CACHE_ADMINISTRATOR_PERMISSIONS_{administratorId}` | `AdministratorPermissionsCacheData` (new) | 15 minutes (safety net) | `AdministratorRepositoryAdapterImpl.findPermissions` | `updateRoles`, `updateStatus` |
| `CACHE_ADMINISTRATOR_LOGIN_FAILURES_{username}` | `LoginFailureCacheData` (new) | 15 minutes (`LOCKOUT_WINDOW_MINUTES`) | `LoginUseCase` on each failed attempt | `LoginUseCase` on successful login |

---

## 9. Verification

Same as the approved plan (`docs/plan/redis-authorization-authentication-20260724.md` §Verification):

1. `./mvnw clean compile -P=local` — all 6 modules build clean.
2. Live permission revocation: deactivate `user_test` via `PATCH /api/v1/administrators/{id}/status` while an unexpired `user_test` access token exists → the next request with that token is rejected. Re-activate → access restored on the next request, no re-login required.
3. Cache hit path: two consecutive authorized requests for the same administrator → second one served from Redis, not the DB (verify via logs).
4. Brute-force lockout: 5 consecutive wrong-password attempts for `user_test`, then a 6th attempt with the correct password → `E-01-ADMINISTRATOR-0013` instead of success. Clear the Redis key or wait out `LOCKOUT_WINDOW_MINUTES` → login succeeds again.
5. Regression: `admin_test`/`user_test` login, refresh, and logout flows behave exactly as before.

---

## Self-review checklist

- [x] Document metadata and revision history present.
- [x] No ambiguous wording — exact values (`5`, `15 minutes`) stated throughout.
- [x] Every new/changed error code has bilingual user-facing messages.
- [x] Exact cache key formats and TTLs specified.
- [x] Exact method signatures and orchestration steps specified for every changed class.
- [x] File saved in `docs/spec/` per project convention.
