package vn.thanhnd.demo.infrastructure.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Routes to the primary DataSource for write transactions and to a healthy replica
 * (round-robin, via {@link ReplicaHealthChecker}) for {@code @Transactional(readOnly = true)} reads.
 */
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    private static final String PRIMARY_KEY = "primary";

    private final ReplicaHealthChecker replicaHealthChecker;

    public ReplicationRoutingDataSource(ReplicaHealthChecker replicaHealthChecker) {
        this.replicaHealthChecker = replicaHealthChecker;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        return readOnly ? replicaHealthChecker.pickReplicaKey() : PRIMARY_KEY;
    }
}
