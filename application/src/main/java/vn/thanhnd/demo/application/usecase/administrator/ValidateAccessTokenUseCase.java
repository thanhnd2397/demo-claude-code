package vn.thanhnd.demo.application.usecase.administrator;

import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.CacheAdapter;
import vn.thanhnd.demo.domain.adapter.TokenProvider;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.domain.model.TokenClaims;
import vn.thanhnd.demo.util.annotation.UseCase;
import vn.thanhnd.demo.util.constant.ApplicationConstants;

/**
 * Parses and verifies a bearer access token for {@code JwtAuthenticationFilter}: checks
 * signature/expiry via {@link TokenProvider} and rejects tokens blacklisted on logout.
 */
@UseCase
public class ValidateAccessTokenUseCase {

    private final TokenProvider tokenProvider;
    private final CacheAdapter cacheAdapter;

    public ValidateAccessTokenUseCase(TokenProvider tokenProvider, CacheAdapter cacheAdapter) {
        this.tokenProvider = tokenProvider;
        this.cacheAdapter = cacheAdapter;
    }

    /**
     * Execute validate access token operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Parse the token and verify its signature and expiry
     * 2. Reject the token if its jti is blacklisted (logged out) in Redis (E-01-ADMINISTRATOR-0006)
     * 3. Return the administrator id, role names, and permission names carried in the token
     *
     * @param accessToken The Bearer access token to validate
     * @return ResultWrapper with AccessTokenClaims on success, or the domain error code on failure
     */
    // No @Transactional: only touches Redis, never the datasource. Runs on every authenticated request.
    public ResultWrapper<AccessTokenClaims> validate(String accessToken) {
        return ResultHandler.handle(() -> {
            TokenClaims claims = tokenProvider.parse(accessToken);

            if (cacheAdapter.exists(ApplicationConstants.cacheKeyAdministratorTokenBlacklist(claims.jti()))) {
                throw new DomainValidationException("E-01-ADMINISTRATOR-0006");
            }

            return new AccessTokenClaims(claims.administratorId(), claims.roleNames(), claims.permissionNames());
        });
    }
}
