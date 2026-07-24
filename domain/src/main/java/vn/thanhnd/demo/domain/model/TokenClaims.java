package vn.thanhnd.demo.domain.model;

import java.time.Instant;
import java.util.Set;

/**
 * Decoded claims of a JWT issued by {@link vn.thanhnd.demo.domain.adapter.TokenProvider}.
 *
 * @param administratorId subject of the token
 * @param jti             unique token id, used as the blacklist key on logout
 * @param roleNames       roles carried in the token, mapped to granted authorities
 * @param permissionNames permissions carried in the token, mapped to granted authorities
 * @param expiresAt       token expiration instant
 */
public record TokenClaims(
        String administratorId, String jti, Set<String> roleNames, Set<String> permissionNames, Instant expiresAt) {
}
