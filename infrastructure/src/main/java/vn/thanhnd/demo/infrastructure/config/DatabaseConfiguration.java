package vn.thanhnd.demo.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wires the primary/replica routing datasource. {@code @Transactional(readOnly = true)} reads are
 * routed to a healthy replica; writes always go to the primary. See {@link ReplicationRoutingDataSource}.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({DemoDataSourceProperties.class, ReplicaHealthProperties.class})
public class DatabaseConfiguration {

    private static final String PRIMARY_KEY = "primary";

    private final DemoDataSourceProperties properties;

    public DatabaseConfiguration(DemoDataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DataSource primaryDataSource() {
        return buildDataSource(properties.getPrimary());
    }

    @Bean
    public Map<String, DataSource> replicaDataSources() {
        Map<String, DataSource> replicas = new LinkedHashMap<>();
        properties.getReplicas().forEach(node -> replicas.put(node.getKey(), buildDataSource(node)));
        return replicas;
    }

    @Bean
    public ReplicaHealthChecker replicaHealthChecker(
            Map<String, DataSource> replicaDataSources,
            ReplicaHealthProperties healthProperties) {
        return new ReplicaHealthChecker(
                replicaDataSources,
                healthProperties.getFailoverCooldownSeconds(),
                healthProperties.getMaxConsecutiveFailures());
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            Map<String, DataSource> replicaDataSources,
            ReplicaHealthChecker replicaHealthChecker) {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource(replicaHealthChecker);
        Map<Object, Object> targets = new LinkedHashMap<>();
        targets.put(PRIMARY_KEY, primaryDataSource);
        targets.putAll(replicaDataSources);
        routingDataSource.setTargetDataSources(targets);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        // Lazily resolves the target DataSource so determineCurrentLookupKey() runs
        // after the transaction's readOnly flag has been set.
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    private DataSource buildDataSource(DemoDataSourceProperties.Node node) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(node.getUrl());
        dataSource.setUsername(node.getUsername());
        dataSource.setPassword(node.getPassword());
        dataSource.setDriverClassName(node.getDriverClassName());
        return dataSource;
    }
}
