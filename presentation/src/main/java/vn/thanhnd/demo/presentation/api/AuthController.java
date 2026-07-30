package vn.thanhnd.demo.presentation.api;

import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.thanhnd.demo.application.usecase.administrator.LoginRequest;
import vn.thanhnd.demo.application.usecase.administrator.LoginResponse;
import vn.thanhnd.demo.application.usecase.administrator.LoginUseCase;
import vn.thanhnd.demo.application.usecase.administrator.LogoutUseCase;
import vn.thanhnd.demo.application.usecase.administrator.RefreshTokenRequest;
import vn.thanhnd.demo.application.usecase.administrator.RefreshTokenUseCase;
import vn.thanhnd.demo.application.usecase.administrator.RegisterAdministratorRequest;
import vn.thanhnd.demo.application.usecase.administrator.RegisterAdministratorResponse;
import vn.thanhnd.demo.application.usecase.administrator.RegisterAdministratorUseCase;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final LoginUseCase loginUseCase;
    private final RegisterAdministratorUseCase registerAdministratorUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(
            LoginUseCase loginUseCase,
            RegisterAdministratorUseCase registerAdministratorUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            ResponseMaker responseMaker,
            MessageSource messageSource) {
        super(responseMaker, messageSource);
        this.loginUseCase = loginUseCase;
        this.registerAdministratorUseCase = registerAdministratorUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    /**
     * Authenticate an administrator.
     * POST /api/v1/auth/login
     * <p>
     * Verifies the username/password pair against the stored BCrypt hash and issues a new
     * access + refresh token pair. The refresh token is stored in Redis as the current session.
     * Only ACTIVE accounts can log in.
     * Publicly accessible (no Bearer token required).
     *
     * @param request LoginRequest containing username and password
     * @return RestResponse with LoginResponse containing accessToken, refreshToken, and expiresInSeconds
     */
    @PostMapping("/login")
    public ResponseEntity<RestResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return toResponseEntity(loginUseCase.login(request), HttpStatus.OK);
    }

    /**
     * Register a new administrator account.
     * POST /api/v1/auth/register
     * <p>
     * Creates an administrator with ACTIVE status, the default ADMIN role, and a BCrypt-hashed password.
     * Username and email must both be unique.
     * Publicly accessible (no Bearer token required).
     *
     * @param request RegisterAdministratorRequest containing username, email, and password
     * @return RestResponse with RegisterAdministratorResponse containing the created account's id, username, and email (HTTP 201)
     */
    @PostMapping("/register")
    public ResponseEntity<RestResponse<RegisterAdministratorResponse>> register(
            @Valid @RequestBody RegisterAdministratorRequest request) {
        return toResponseEntity(registerAdministratorUseCase.register(request), HttpStatus.CREATED);
    }

    /**
     * Rotate the token pair using a refresh token.
     * POST /api/v1/auth/refresh
     * <p>
     * Validates the refresh token against the Redis-stored session, then issues and stores a new
     * access + refresh token pair (the old refresh token is replaced — single active session per account).
     * Only ACTIVE accounts can refresh.
     * Publicly accessible (authentication is the refresh token itself, no Bearer token required).
     *
     * @param request RefreshTokenRequest containing the current refresh token
     * @return RestResponse with LoginResponse containing the new accessToken, refreshToken, and expiresInSeconds
     */
    @PostMapping("/refresh")
    public ResponseEntity<RestResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return toResponseEntity(refreshTokenUseCase.refresh(request), HttpStatus.OK);
    }

    /**
     * Log out the authenticated administrator.
     * POST /api/v1/auth/logout
     * <p>
     * Deletes the Redis-stored refresh token session and blacklists the presented access token's jti
     * until its natural expiry, so the token can no longer be used.
     * Requires Bearer token.
     *
     * @param authorizationHeader The Authorization header carrying the Bearer access token
     * @return RestResponse with empty data on success
     */
    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());
        return toResponseEntity(logoutUseCase.logout(accessToken), HttpStatus.OK);
    }
}
