# Plan: Redis-backed authorization (live permission cache) + login brute-force lockout

## Document metadata

| Field | Value |
|-------|-------|
| Created date | 2026/07/24 |
| Created by | Claude Code |
| Approved date | 2026/07/24 |
| Approved by | thanhnd |
| Spec ref | Adhoc — no spec doc |
| Version | 1.0 |

## Context

Redis is already used for **authentication** concerns only: `RefreshTokenCacheData` (session/rotation) and `TokenBlacklistCacheData` (logout revocation) — see `ApplicationConstants.cacheKeyAdministratorRefreshToken/TokenBlacklist`. Redis is **never** used for authorization: role/permission names are baked into the JWT at issue time (`JwtTokenProviderImpl.issueToken`) and trusted as-is by `JwtAuthenticationFilter` for the token's entire lifetime.

This creates a real gap: if an admin's roles/status are changed via `PATCH /administrators/{id}/roles|status`, the change has **no effect** until the caller's access token naturally expires (default 30 min, `JwtProperties.accessTokenExpirationMinutes`) or they call `/auth/refresh` (which does re-derive fresh permissions from the DB). A de-provisioned or demoted admin keeps their old authority for up to 30 minutes.

The user asked whether Redis can be applied to authentication and authorization, and chose:
1. **Authorization** — cache the administrator's live role/permission set in Redis (cache-aside), invalidated on write, so authorization checks always reflect the current DB state instead of trusting stale JWT claims.
2. **Authentication** — add Redis-backed brute-force lockout on login (failed-attempt counter + temporary lockout).

## Approach

### 1. Live permission cache (authorization)

The JWT continues to prove **identity** (subject = administratorId, signature, expiry) and is still checked against the existing blacklist. What changes is where **authorization data** (roles/permissions) comes from: instead of trusting the JWT's embedded claims, `ValidateAccessTokenUseCase` looks up the administrator's *current* roles/permissions via a new cache-aside port method. `JwtAuthenticationFilter` and `AccessTokenClaims` need **no changes** — they already just consume whatever `roleNames()`/`permissionNames()` the use case returns.

- **New cache key**: `ApplicationConstants.cacheKeyAdministratorPermissions(administratorId)` → `CACHE_ADMINISTRATOR_PERMISSIONS_{administratorId}`.
- **New `CacheData` type**: `domain/model/AdministratorPermissionsCacheData(Set<String> roleNames, Set<String> permissionNames) implements CacheData`, `@JsonTypeName("administratorPermissionsCacheData")` — same pattern as `RefreshTokenCacheData`/`TokenBlacklistCacheData`.
- **`AdministratorRepositoryPort`**: add `AdministratorPermissionsCacheData findPermissions(String administratorId)`.
  - `@throws DomainValidationException` (new code `E-01-ADMINISTRATOR-0012`) if the administrator no longer exists — reachable if a valid, non-expired JWT outlives its administrator being deleted.
- **`AdministratorRepositoryAdapterImpl.findPermissions(...)`** (cache-aside):
  1. `cacheAdapter.get(key, AdministratorPermissionsCacheData.class)` — return on hit.
  2. On miss: `findEntityOrThrow(administratorId)` (reuse existing helper) → `administratorMapper.toDomain(entity)` → build `AdministratorPermissionsCacheData` from `.roleNames()`/`.permissionNames()` (mapper already computes these) → `cacheAdapter.set(key, data, expiredAt)` with a TTL of ~15 minutes (safety net only — the common case is explicit invalidation below) → return.
  - Requires injecting `CacheAdapter` into this adapter (currently only has `AdministratorJpaRepository`, `RoleJpaRepository`, `AdministratorMapper`).
- **Cache invalidation**: in `updateRoles(...)` and `updateStatus(...)`, after `administratorJpaRepository.save(entity)`, call `cacheAdapter.delete(cacheKeyAdministratorPermissions(administratorId))` before returning — matches the project's documented "invalidate on writes" cache strategy.
- **`ValidateAccessTokenUseCase`**: add `AdministratorRepositoryPort` dependency. New flow:
  1. `tokenProvider.parse(accessToken)` (unchanged — signature/expiry check).
  2. Blacklist check (unchanged).
  3. **New**: `administratorRepositoryPort.findPermissions(claims.administratorId())` → live roles/permissions.
  4. Return `AccessTokenClaims(claims.administratorId(), live.roleNames(), live.permissionNames())`.

**Result**: a role/status change takes effect on the **next request** after cache invalidation (near-instant), not after up to 30 minutes.

**Note (documented, not actioned)**: the JWT still carries `roles`/`permissions` claims (`JwtTokenProviderImpl`) — these become unused for authorization decisions after this change. Left as-is to keep this change surgical; removing them would touch `Administrator`, `RegisterAdministratorUseCase`, `TokenClaims`, and token issuance, which is a separate cleanup not required for this feature to work.

### 2. Login brute-force lockout (authentication)

- **New cache key**: `ApplicationConstants.cacheKeyAdministratorLoginFailures(username)` → `CACHE_ADMINISTRATOR_LOGIN_FAILURES_{username}`.
- **New `CacheData` type**: `domain/model/LoginFailureCacheData(int attemptCount) implements CacheData`, `@JsonTypeName("loginFailureCacheData")`.
- **`LoginUseCase.login(...)`** — add, as the first steps before the existing username/password check:
  1. Read the current failure count from cache (miss = 0).
  2. If `attemptCount >= MAX_FAILED_ATTEMPTS` (constant, `5`) → throw new `DomainValidationException("E-01-ADMINISTRATOR-0013")` (account temporarily locked).
  3. On password mismatch (existing `E-01-ADMINISTRATOR-0003` path) → increment and store the counter with TTL = `LOCKOUT_WINDOW_MINUTES` (constant, `15`), then rethrow.
  4. On success → `cacheAdapter.delete(...)` to reset the counter, then continue the existing flow unchanged.
- Constants (`MAX_FAILED_ATTEMPTS`, `LOCKOUT_WINDOW_MINUTES`) are hardcoded `private static final` fields in `LoginUseCase` — no new configurability requested, matches Simplicity First.

## Files to touch

| File | Change |
|---|---|
| `domain/model/AdministratorPermissionsCacheData.java` (new) | Cacheable roles+permissions record |
| `domain/model/LoginFailureCacheData.java` (new) | Cacheable login-failure counter record |
| `domain/adapter/AdministratorRepositoryPort.java` | Add `findPermissions(String administratorId)` |
| `infrastructure/adapter/AdministratorRepositoryAdapterImpl.java` | Implement `findPermissions` (cache-aside); inject `CacheAdapter`; invalidate cache in `updateRoles`/`updateStatus` |
| `application/usecase/administrator/ValidateAccessTokenUseCase.java` | Depend on `AdministratorRepositoryPort`; source roles/permissions from `findPermissions` instead of JWT claims |
| `application/usecase/administrator/LoginUseCase.java` | Add brute-force counter check/increment/reset around the existing password check |
| `util/constant/ApplicationConstants.java` | Add `cacheKeyAdministratorPermissions`, `cacheKeyAdministratorLoginFailures` |
| New error codes | `E-01-ADMINISTRATOR-0012` (administrator not found during token validation), `E-01-ADMINISTRATOR-0013` (account temporarily locked) |

No changes needed to `JwtAuthenticationFilter`, `AccessTokenClaims`, `TokenProvider`/`JwtTokenProviderImpl`, `SecurityConfig`, or any controller.

## Verification

1. `./mvnw clean compile -P=local` — all 6 modules build clean.
2. **Live permission revocation**: log in as `admin_test`, call `PATCH /administrators/{user_test-id}/status` to deactivate `user_test`, then immediately retry a request using an *already-issued, still-unexpired* `user_test` access token → expect it to now be rejected (previously would have kept working until token expiry). Re-activate and confirm access is restored on the next request without needing `user_test` to re-login.
3. **Cache hit path**: repeat an authorized request twice in a row for the same administrator; confirm (via logs or a temporary counter) the second call is served from Redis, not the DB.
4. **Brute-force lockout**: submit `POST /auth/login` with a wrong password for `user_test` 5 times → 6th attempt (even with the *correct* password) returns the new locked-account error instead of succeeding. Wait for the lockout window or manually clear the Redis key, then confirm login succeeds again.
5. Confirm existing behavior is unchanged: `admin_test`/`user_test` login, refresh, and logout flows still work exactly as before.
