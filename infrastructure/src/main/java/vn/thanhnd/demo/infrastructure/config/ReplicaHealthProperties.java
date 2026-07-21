package vn.thanhnd.demo.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for database replica health check and failover settings.
 */
@Data
@ConfigurationProperties(prefix = "datasource.replica")
public class ReplicaHealthProperties {

    /**
     * Interval in milliseconds between replica health checks.
     */
    private long healthCheckIntervalMs = 30000;

    /**
     * Cooldown duration in seconds for an unhealthy replica before it can be used again.
     */
    private long failoverCooldownSeconds = 60;

    /**
     * Number of consecutive health check failures required to mark a replica as unhealthy.
     */
    private int maxConsecutiveFailures = 3;
}
