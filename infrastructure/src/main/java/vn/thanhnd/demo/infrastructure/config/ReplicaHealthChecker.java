package vn.thanhnd.demo.infrastructure.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Periodically pings each replica DataSource and keeps unhealthy replicas in a cooldown window.
 * {@link #pickReplicaKey()} round-robins over the currently healthy replicas and falls back
 * to {@code "primary"} when none are healthy.
 */
@Log4j2
public class ReplicaHealthChecker {

    private static final String PRIMARY_KEY = "primary";

    private final Map<String, DataSource> replicas;
    private final long cooldownMillis;
    private final int maxConsecutiveFailures;

    private final Map<String, AtomicInteger> failureCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> cooldownUntil = new ConcurrentHashMap<>();
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public ReplicaHealthChecker(Map<String, DataSource> replicas, long cooldownSeconds, int maxConsecutiveFailures) {
        this.replicas = replicas;
        this.cooldownMillis = cooldownSeconds * 1000L;
        this.maxConsecutiveFailures = maxConsecutiveFailures;
        replicas.keySet().forEach(key -> failureCounts.put(key, new AtomicInteger(0)));
    }

    @Scheduled(fixedDelayString = "${datasource.replica.health-check-interval-ms:30000}")
    public void checkReplicaHealth() {
        replicas.forEach((key, dataSource) -> {
            if (isHealthy(dataSource)) {
                failureCounts.get(key).set(0);
                cooldownUntil.remove(key);
            } else {
                int failures = failureCounts.get(key).incrementAndGet();
                if (failures >= maxConsecutiveFailures) {
                    Instant until = Instant.now().plusMillis(cooldownMillis);
                    cooldownUntil.put(key, until);
                    log.warn("Replica '{}' marked unhealthy after {} consecutive failures; cooldown until {}",
                            key, failures, until);
                }
            }
        });
    }

    public String pickReplicaKey() {
        List<String> healthyKeys = replicas.keySet().stream()
                .filter(key -> !isInCooldown(key))
                .toList();
        if (healthyKeys.isEmpty()) {
            return PRIMARY_KEY;
        }
        int index = Math.floorMod(roundRobinIndex.getAndIncrement(), healthyKeys.size());
        return healthyKeys.get(index);
    }

    private boolean isInCooldown(String key) {
        Instant until = cooldownUntil.get(key);
        return until != null && Instant.now().isBefore(until);
    }

    private boolean isHealthy(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
