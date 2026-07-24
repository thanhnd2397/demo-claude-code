package vn.thanhnd.demo.application.usecase.administrator;

import org.springframework.transaction.annotation.Transactional;
import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.adapter.CacheAdapter;
import vn.thanhnd.demo.domain.adapter.TokenProvider;
import vn.thanhnd.demo.domain.cache.Cache;
import vn.thanhnd.demo.domain.enums.AdministratorStatus;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.domain.model.RefreshTokenCacheData;
import vn.thanhnd.demo.domain.model.TokenClaims;
import vn.thanhnd.demo.util.annotation.UseCase;
import vn.thanhnd.demo.util.constant.ApplicationConstants;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Validates a refresh token against the Redis-stored session and rotates it, issuing a new
 * access + refresh token pair.
 */
@UseCase
public class RefreshTokenUseCase {

    private final AdministratorRepositoryPort administratorRepositoryPort;
    private final TokenProvider tokenProvider;
    private final CacheAdapter cacheAdapter;

    public RefreshTokenUseCase(
            AdministratorRepositoryPort administratorRepositoryPort,
            TokenProvider tokenProvider,
            CacheAdapter cacheAdapter) {
        this.administratorRepositoryPort = administratorRepositoryPort;
        this.tokenProvider = tokenProvider;
        this.cacheAdapter = cacheAdapter;
    }

    /**
     * Execute refresh token operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Parse and verify the refresh token's signature and expiry
     * 2. Compare it against the Redis-stored session for that administrator (E-01-ADMINISTRATOR-0005 on mismatch)
     * 3. Validate the administrator still exists and is ACTIVE (E-01-ADMINISTRATOR-0005 otherwise)
     * 4. Issue a new access + refresh token pair
     * 5. Replace the stored refresh token with the new one (rotation — old token becomes unusable)
     * 6. Return both tokens with the access token's remaining lifetime in seconds
     *
     * @param request RefreshTokenRequest containing the current refresh token
     * @return ResultWrapper with LoginResponse on success, or the domain error code on failure
     */
    @Transactional(readOnly = true)
    public ResultWrapper<LoginResponse> refresh(RefreshTokenRequest request) {
        return ResultHandler.handle(() -> {
            TokenClaims refreshClaims = tokenProvider.parse(request.refreshToken());
            String cacheKey = ApplicationConstants.cacheKeyAdministratorRefreshToken(refreshClaims.administratorId());
            Cache cache = cacheAdapter.get(cacheKey, Cache.class);

            if (!(cache != null && cache.getData() instanceof RefreshTokenCacheData stored)
                    || !stored.refreshToken().equals(request.refreshToken())) {
                throw new DomainValidationException("E-01-ADMINISTRATOR-0005");
            }

            Administrator administrator = administratorRepositoryPort.findById(refreshClaims.administratorId())
                    .filter(candidate -> candidate.status() == AdministratorStatus.ACTIVE)
                    .orElseThrow(() -> new DomainValidationException("E-01-ADMINISTRATOR-0005"));

            String newAccessToken = tokenProvider.issueAccessToken(administrator);
            String newRefreshToken = tokenProvider.issueRefreshToken(administrator);
            TokenClaims newAccessClaims = tokenProvider.parse(newAccessToken);
            TokenClaims newRefreshClaims = tokenProvider.parse(newRefreshToken);

            cacheAdapter.set(
                    cacheKey,
                    new RefreshTokenCacheData(administrator.id(), newRefreshToken),
                    LocalDateTime.ofInstant(newRefreshClaims.expiresAt(), ZoneId.systemDefault()));

            long expiresInSeconds = Duration.between(Instant.now(), newAccessClaims.expiresAt()).getSeconds();
            return new LoginResponse(newAccessToken, newRefreshToken, expiresInSeconds);
        });
    }
}
