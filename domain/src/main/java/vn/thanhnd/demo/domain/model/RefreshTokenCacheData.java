package vn.thanhnd.demo.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import vn.thanhnd.demo.domain.cache.CacheData;

/**
 * Cached refresh token issued to an administrator, keyed by
 * {@code CACHE_ADMINISTRATOR_REFRESH_TOKEN_{administratorId}}. Used to validate rotation on refresh
 * and to invalidate the session on logout.
 */
@JsonTypeName("refreshTokenCacheData")
public record RefreshTokenCacheData(String administratorId, String refreshToken) implements CacheData {
}
