package vn.thanhnd.demo.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import vn.thanhnd.demo.domain.cache.CacheData;

/**
 * Marks an access token's {@code jti} as revoked, keyed by
 * {@code CACHE_ADMINISTRATOR_TOKEN_BLACKLIST_{jti}}, until the token's natural expiry.
 */
@JsonTypeName("tokenBlacklistCacheData")
public record TokenBlacklistCacheData(String jti) implements CacheData {
}
