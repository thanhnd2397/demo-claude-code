package vn.thanhnd.demo.application.usecase.administrator;

import org.springframework.transaction.annotation.Transactional;
import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.adapter.CacheAdapter;
import vn.thanhnd.demo.domain.adapter.PasswordHasher;
import vn.thanhnd.demo.domain.adapter.TokenProvider;
import vn.thanhnd.demo.domain.enums.AdministratorStatus;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.domain.model.LoginFailureCacheData;
import vn.thanhnd.demo.domain.model.RefreshTokenCacheData;
import vn.thanhnd.demo.domain.model.TokenClaims;
import vn.thanhnd.demo.util.annotation.UseCase;
import vn.thanhnd.demo.util.constant.ApplicationConstants;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Authenticates an administrator by username/password and issues an access + refresh token pair.
 */
@UseCase
public class LoginUseCase {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_WINDOW_MINUTES = 15;

    private final AdministratorRepositoryPort administratorRepositoryPort;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final CacheAdapter cacheAdapter;

    public LoginUseCase(
            AdministratorRepositoryPort administratorRepositoryPort,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider,
            CacheAdapter cacheAdapter) {
        this.administratorRepositoryPort = administratorRepositoryPort;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.cacheAdapter = cacheAdapter;
    }

    /**
     * Execute login operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Reject the login if the account has reached the failed-attempt lockout threshold (E-01-ADMINISTRATOR-0013)
     * 2. Find administrator by username and verify the password against the stored BCrypt hash; on mismatch, increment the failed-attempt counter in Redis and throw (E-01-ADMINISTRATOR-0003)
     * 3. Validate the account status is ACTIVE (E-01-ADMINISTRATOR-0004 otherwise)
     * 4. Reset the failed-attempt counter in Redis
     * 5. Issue a new access token and refresh token
     * 6. Store the refresh token in Redis keyed by administrator id, expiring with the token
     * 7. Return both tokens with the access token's remaining lifetime in seconds
     *
     * @param request LoginRequest containing username and password
     * @return ResultWrapper with LoginResponse on success, or the domain error code on failure
     */
    @Transactional(readOnly = true)
    public ResultWrapper<LoginResponse> login(LoginRequest request) {
        return ResultHandler.handle(() -> {
            String failureKey = ApplicationConstants.cacheKeyAdministratorLoginFailures(request.username());
            LoginFailureCacheData failures = cacheAdapter.get(failureKey, LoginFailureCacheData.class);
            if (failures != null && failures.attemptCount() >= MAX_FAILED_ATTEMPTS) {
                throw new DomainValidationException("E-01-ADMINISTRATOR-0013");
            }

            Administrator administrator = administratorRepositoryPort.findByUsername(request.username())
                    .filter(candidate -> passwordHasher.matches(request.password(), candidate.passwordHash()))
                    .orElseThrow(() -> {
                        int nextCount = (failures == null ? 0 : failures.attemptCount()) + 1;
                        cacheAdapter.set(
                                failureKey,
                                new LoginFailureCacheData(nextCount),
                                LocalDateTime.now().plusMinutes(LOCKOUT_WINDOW_MINUTES));
                        return new DomainValidationException("E-01-ADMINISTRATOR-0003");
                    });

            if (administrator.status() != AdministratorStatus.ACTIVE) {
                throw new DomainValidationException("E-01-ADMINISTRATOR-0004");
            }

            cacheAdapter.delete(failureKey);

            String accessToken = tokenProvider.issueAccessToken(administrator);
            String refreshToken = tokenProvider.issueRefreshToken(administrator);
            TokenClaims accessClaims = tokenProvider.parse(accessToken);
            TokenClaims refreshClaims = tokenProvider.parse(refreshToken);

            cacheAdapter.set(
                    ApplicationConstants.cacheKeyAdministratorRefreshToken(administrator.id()),
                    new RefreshTokenCacheData(administrator.id(), refreshToken),
                    LocalDateTime.ofInstant(refreshClaims.expiresAt(), ZoneId.systemDefault()));

            long expiresInSeconds = Duration.between(Instant.now(), accessClaims.expiresAt()).getSeconds();
            return new LoginResponse(accessToken, refreshToken, expiresInSeconds);
        });
    }
}
