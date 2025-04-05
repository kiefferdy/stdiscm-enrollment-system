package com.stdiscm.common.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A routing datasource that switches between primary and secondary database
 * based on whether the current transaction is read-only.
 * Read operations use the secondary database, write operations use the primary.
 * This provides load balancing and redundancy.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    public static final String PRIMARY = "primary";
    public static final String SECONDARY = "secondary";

    @Override
    protected Object determineCurrentLookupKey() {
        // Use secondary datasource for read-only operations
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? SECONDARY : PRIMARY;
    }
}
