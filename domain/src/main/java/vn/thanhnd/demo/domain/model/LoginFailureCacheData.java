package vn.thanhnd.demo.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import vn.thanhnd.demo.domain.cache.CacheData;

/**
 * Tracks consecutive failed login attempts for a username, keyed by
 * {@code CACHE_ADMINISTRATOR_LOGIN_FAILURES_{username}}, to enforce a temporary lockout
 * after too many failures within the tracking window.
 */
@JsonTypeName("loginFailureCacheData")
public record LoginFailureCacheData(int attemptCount) implements CacheData {
}
