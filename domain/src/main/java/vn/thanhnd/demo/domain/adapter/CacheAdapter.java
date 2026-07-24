package vn.thanhnd.demo.domain.adapter;

import vn.thanhnd.demo.domain.cache.CacheData;

import java.time.LocalDateTime;

/**
 * Port for cache-aside read/write operations (backed by Redis in infrastructure).
 */
public interface CacheAdapter {

    /**
     * Read a cached value.
     *
     * @param key The cache key
     * @param type The expected value type
     * @return The cached value, or null on a cache miss
     */
    <T> T get(String key, Class<T> type);

    /**
     * Store a value with an absolute expiry time.
     *
     * @param key The cache key
     * @param data The value to cache
     * @param expiredAt When the entry expires
     */
    void set(String key, CacheData data, LocalDateTime expiredAt);

    /**
     * Remove a single cache entry.
     *
     * @param key The cache key to remove
     */
    void delete(String key);

    /**
     * Remove all cache entries whose keys match the given pattern.
     *
     * @param pattern The key pattern (Redis glob style, e.g. {@code CACHE_ADMINISTRATOR_*})
     */
    void deleteByPattern(String pattern);

    /**
     * Check whether a cache entry exists.
     *
     * @param key The cache key
     * @return true if the key exists and has not expired
     */
    boolean exists(String key);
}
