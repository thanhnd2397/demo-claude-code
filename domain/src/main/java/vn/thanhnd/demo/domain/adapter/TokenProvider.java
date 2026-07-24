package vn.thanhnd.demo.domain.adapter;

import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.domain.model.TokenClaims;

/**
 * Port for issuing and parsing JWT access/refresh tokens (backed by jjwt in infrastructure).
 */
public interface TokenProvider {

    /**
     * Issue a short-lived access token carrying the administrator's id, role names, and permission names.
     *
     * @param administrator The authenticated administrator
     * @return The signed access token
     */
    String issueAccessToken(Administrator administrator);

    /**
     * Issue a long-lived refresh token for the administrator.
     *
     * @param administrator The authenticated administrator
     * @return The signed refresh token
     */
    String issueRefreshToken(Administrator administrator);

    /**
     * Parse a token and verify its signature and expiry.
     *
     * @param token The token to parse
     * @return The claims carried by the token (administrator id, jti, roles, permissions, expiry)
     * @throws vn.thanhnd.demo.domain.exception.DomainValidationException if the token is malformed, expired, or has an invalid signature
     */
    TokenClaims parse(String token);
}
