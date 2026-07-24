package vn.thanhnd.demo.infrastructure.adapter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import vn.thanhnd.demo.domain.adapter.TokenProvider;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.domain.model.TokenClaims;
import vn.thanhnd.demo.infrastructure.config.JwtProperties;
import vn.thanhnd.demo.util.annotation.Adapter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Adapter
public class JwtTokenProviderImpl implements TokenProvider {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProviderImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issueAccessToken(Administrator administrator) {
        return issueToken(administrator, jwtProperties.getAccessTokenExpirationMinutes());
    }

    @Override
    public String issueRefreshToken(Administrator administrator) {
        return issueToken(administrator, jwtProperties.getRefreshTokenExpirationMinutes());
    }

    @Override
    public TokenClaims parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            List<?> rawRoles = claims.get(CLAIM_ROLES, List.class);
            Set<String> roleNames = new HashSet<>();
            if (rawRoles != null) {
                rawRoles.forEach(role -> roleNames.add(String.valueOf(role)));
            }

            List<?> rawPermissions = claims.get(CLAIM_PERMISSIONS, List.class);
            Set<String> permissionNames = new HashSet<>();
            if (rawPermissions != null) {
                rawPermissions.forEach(permission -> permissionNames.add(String.valueOf(permission)));
            }

            return new TokenClaims(
                    claims.getSubject(), claims.getId(), roleNames, permissionNames, claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainValidationException("E-01-ADMINISTRATOR-0007", e);
        }
    }

    private String issueToken(Administrator administrator, long expirationMinutes) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(administrator.id())
                .claim(CLAIM_ROLES, administrator.roleNames())
                .claim(CLAIM_PERMISSIONS, administrator.permissionNames())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}
