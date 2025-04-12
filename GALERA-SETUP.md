# Galera Cluster with ProxySQL Setup Guide

This document provides comprehensive instructions for running the Enrollment System with Galera Cluster for database redundancy and ProxySQL for connection management.

## Architecture Overview

The database tier consists of:

1. **3-Node Galera Cluster**: True multi-master replication where all nodes can accept both reads and writes
2. **ProxySQL**: MySQL-aware load balancer that routes queries intelligently across the cluster
3. **Application Services**: Connect to the database through ProxySQL with no code changes required

```
Application Services → ProxySQL → Galera Cluster (3 nodes)
```

## Components

### Galera Cluster

- **galera-node1**: Primary node that bootstraps the cluster
- **galera-node2**: Secondary node that joins the cluster
- **galera-node3**: Tertiary node that joins the cluster

All nodes contain identical data due to synchronous replication.

### ProxySQL

ProxySQL distributes database queries across the Galera Cluster nodes according to these rules:

- Distributes SELECT queries across all nodes
- Routes write operations across all nodes (using Galera's built-in conflict resolution)
- Monitors node health and routes traffic only to healthy nodes
- Maintains connection pools for improved performance

## Running the System

To start the system with Galera Cluster and ProxySQL:

```bash
docker-compose -f docker-compose-galera.yml up -d
```

The first startup might take longer as the Galera Cluster initializes. Monitor the logs to ensure everything starts correctly:

```bash
docker-compose -f docker-compose-galera.yml logs -f
```

## Verifying the Setup

### 1. Check Galera Cluster Status

```bash
docker exec -it galera-node1 mysql -uroot -proot -e "SHOW STATUS LIKE 'wsrep_%';"
```

Look for:
- `wsrep_cluster_size`: Should be 3
- `wsrep_cluster_status`: Should be "Primary"
- `wsrep_ready`: Should be "ON"

### 2. Check ProxySQL Configuration

```bash
docker exec -it proxysql mysql -h127.0.0.1 -P6032 -uadmin -padmin -e "SELECT * FROM mysql_servers;"
```

This should show all three Galera nodes configured in ProxySQL.

### 3. Test Connection Through ProxySQL

```bash
docker exec -it proxysql mysql -h127.0.0.1 -P6033 -uroot -proot -e "SELECT @@hostname;"
```

Running this command multiple times should show different hostnames as ProxySQL routes to different nodes.

### 4. Connect to Any Galera Node Directly

```bash
docker exec -it galera-node1 mysql -uroot -proot -e "SHOW DATABASES;"
```

You should see all your enrollment system databases.

## Benefits of This Configuration

- **High Availability**: System continues to operate even if a database node fails
- **Scalability**: Handles more concurrent users during peak enrollment periods
- **Zero Downtime Maintenance**: Nodes can be updated one at a time without downtime
- **Consistent Performance**: Load is balanced across all database nodes
- **Connection Pooling**: Improved performance through connection reuse
- **Automatic Failover**: ProxySQL detects failures and routes around them

## Monitoring

Monitor the health and performance of your Galera Cluster using:

```bash
docker exec -it galera-node1 mysql -uroot -proot -e "SHOW GLOBAL STATUS LIKE 'wsrep_%';"
```

For ProxySQL monitoring:

```bash
docker exec -it proxysql mysql -h127.0.0.1 -P6032 -uadmin -padmin -e "SELECT * FROM stats.stats_mysql_connection_pool;"
```

## Troubleshooting

If you encounter issues:

1. **Check Galera Cluster Status**:
   ```bash
   docker exec -it galera-node1 mysql -uroot -proot -e "SHOW STATUS LIKE 'wsrep_cluster_%';"
   ```

2. **View ProxySQL Logs**:
   ```bash
   docker logs proxysql
   ```

3. **Check Service Connectivity**:
   ```bash
   docker logs auth-service
   ```

4. **Restart ProxySQL**:
   ```bash
   docker-compose -f docker-compose-galera.yml restart proxysql
   ```

5. **If a Node Falls Out of Sync**:
   ```bash
   docker-compose -f docker-compose-galera.yml restart galera-node2
   ```

6. **Reset the Entire Setup**:
   ```bash
   docker-compose -f docker-compose-galera.yml down -v
   docker-compose -f docker-compose-galera.yml up -d
   ```
   Note: This will remove all data in the databases.
