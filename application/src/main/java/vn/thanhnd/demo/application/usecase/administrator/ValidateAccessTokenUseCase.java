package vn.thanhnd.demo.application.usecase.administrator;

import org.springframework.transaction.annotation.Transactional;
import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.adapter.CacheAdapter;
import vn.thanhnd.demo.domain.adapter.TokenProvider;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.domain.model.AdministratorPermissionsCacheData;
import vn.thanhnd.demo.domain.model.TokenClaims;
import vn.thanhnd.demo.util.annotation.UseCase;
import vn.thanhnd.demo.util.constant.ApplicationConstants;

/**
 * Parses and verifies a bearer access token for {@code JwtAuthenticationFilter}: checks
 * signature/expiry via {@link TokenProvider}, rejects tokens blacklisted on logout, and
 * resolves the administrator's current roles/permissions (cache-aside via Redis) rather than
 * trusting the token's embedded claims, so role/status changes take effect without re-login.
 */
@UseCase
public class ValidateAccessTokenUseCase {

    private final TokenProvider tokenProvider;
    private final CacheAdapter cacheAdapter;
    private final AdministratorRepositoryPort administratorRepositoryPort;

    public ValidateAccessTokenUseCase(
            TokenProvider tokenProvider, CacheAdapter cacheAdapter, AdministratorRepositoryPort administratorRepositoryPort) {
        this.tokenProvider = tokenProvider;
        this.cacheAdapter = cacheAdapter;
        this.administratorRepositoryPort = administratorRepositoryPort;
    }

    /**
     * Execute validate access token operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Parse the token and verify its signature and expiry
     * 2. Reject the token if its jti is blacklisted (logged out) in Redis (E-01-ADMINISTRATOR-0006)
     * 3. Fetch the administrator's current role and permission names (cache-aside via Redis, not from the token)
     * 4. Return the administrator id with those live role and permission names
     *
     * @param accessToken The Bearer access token to validate
     * @return ResultWrapper with AccessTokenClaims on success, or the domain error code on failure
     */
    @Transactional(readOnly = true)
    public ResultWrapper<AccessTokenClaims> validate(String accessToken) {
        return ResultHandler.handle(() -> {
            TokenClaims claims = tokenProvider.parse(accessToken);

            if (cacheAdapter.exists(ApplicationConstants.cacheKeyAdministratorTokenBlacklist(claims.jti()))) {
                throw new DomainValidationException("E-01-ADMINISTRATOR-0006");
            }

            AdministratorPermissionsCacheData live = administratorRepositoryPort.findPermissions(claims.administratorId());
            return new AccessTokenClaims(claims.administratorId(), live.roleNames(), live.permissionNames());
        });
    }
}
