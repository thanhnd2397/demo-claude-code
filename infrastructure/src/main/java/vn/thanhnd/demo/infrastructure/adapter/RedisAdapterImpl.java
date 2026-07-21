package vn.thanhnd.demo.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import vn.thanhnd.demo.domain.adapter.CacheAdapter;
import vn.thanhnd.demo.domain.cache.Cache;
import vn.thanhnd.demo.domain.cache.CacheData;
import vn.thanhnd.demo.infrastructure.exception.RedisException;
import vn.thanhnd.demo.util.annotation.Adapter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Adapter
@RequiredArgsConstructor
@Log4j2
public class RedisAdapterImpl implements CacheAdapter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return type.isInstance(value) ? type.cast(value) : null;
        } catch (Exception e) {
            throw new RedisException("E-03-REDIS-0001", e);
        }
    }

    @Override
    public void set(String key, CacheData data, LocalDateTime expiredAt) {
        try {
            Cache cache = Cache.builder().key(key).expiredAt(expiredAt).data(data).build();
            long ttlSeconds = Math.max(1, Duration.between(LocalDateTime.now(), expiredAt).getSeconds());
            redisTemplate.opsForValue().set(key, cache, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RedisException("E-03-REDIS-0002", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RedisException("E-03-REDIS-0003", e);
        }
    }

    @Override
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            throw new RedisException("E-03-REDIS-0004", e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            throw new RedisException("E-03-REDIS-0005", e);
        }
    }
}
