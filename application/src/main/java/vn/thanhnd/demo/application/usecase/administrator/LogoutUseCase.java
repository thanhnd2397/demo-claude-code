package vn.thanhnd.demo.application.usecase.administrator;

import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.CacheAdapter;
import vn.thanhnd.demo.domain.adapter.TokenProvider;
import vn.thanhnd.demo.domain.model.TokenBlacklistCacheData;
import vn.thanhnd.demo.domain.model.TokenClaims;
import vn.thanhnd.demo.util.annotation.UseCase;
import vn.thanhnd.demo.util.constant.ApplicationConstants;

import java.time.ZoneId;
import java.time.LocalDateTime;

/**
 * Invalidates the caller's session: deletes the stored refresh token and blacklists the
 * current access token's {@code jti} until its natural expiry.
 */
@UseCase
public class LogoutUseCase {

    private final TokenProvider tokenProvider;
    private final CacheAdapter cacheAdapter;

    public LogoutUseCase(TokenProvider tokenProvider, CacheAdapter cacheAdapter) {
        this.tokenProvider = tokenProvider;
        this.cacheAdapter = cacheAdapter;
    }

    /**
     * Execute logout operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Parse and verify the access token to extract administrator id and jti
     * 2. Delete the administrator's stored refresh token session from Redis
     * 3. Blacklist the access token's jti in Redis until the token's natural expiry
     *
     * @param accessToken The Bearer access token presented by the caller
     * @return ResultWrapper with no data on success, or the domain error code on failure
     */
    // No @Transactional: only touches Redis, never the datasource.
    public ResultWrapper<Void> logout(String accessToken) {
        return ResultHandler.handle(() -> {
            TokenClaims claims = tokenProvider.parse(accessToken);

            cacheAdapter.delete(ApplicationConstants.cacheKeyAdministratorRefreshToken(claims.administratorId()));
            cacheAdapter.set(
                    ApplicationConstants.cacheKeyAdministratorTokenBlacklist(claims.jti()),
                    new TokenBlacklistCacheData(claims.jti()),
                    LocalDateTime.ofInstant(claims.expiresAt(), ZoneId.systemDefault()));

            return null;
        });
    }
}
