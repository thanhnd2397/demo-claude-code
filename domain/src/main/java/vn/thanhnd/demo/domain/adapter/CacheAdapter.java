package vn.thanhnd.demo.domain.adapter;

import vn.thanhnd.demo.domain.cache.CacheData;

import java.time.LocalDateTime;

/**
 * Port for cache-aside read/write operations (backed by Redis in infrastructure).
 */
public interface CacheAdapter {

    <T> T get(String key, Class<T> type);

    void set(String key, CacheData data, LocalDateTime expiredAt);

    void delete(String key);

    void deleteByPattern(String pattern);

    boolean exists(String key);
}
