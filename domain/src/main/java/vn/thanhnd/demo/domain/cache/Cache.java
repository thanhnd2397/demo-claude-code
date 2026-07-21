package vn.thanhnd.demo.domain.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mutable envelope stored in Redis: a cache key, its expiration time, and the payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cache {

    private String key;
    private LocalDateTime expiredAt;
    private CacheData data;
}
