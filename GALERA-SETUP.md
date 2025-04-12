# Galera Cluster with ProxySQL Setup Guide

This document provides instructions for running the Enrollment System with Galera Cluster for database redundancy and ProxySQL for connection management.

## Overview

This implementation provides:
- 3-node Galera Cluster for true multi-master replication
- ProxySQL for intelligent query routing
- Zero code changes to the application services
- Full redundancy and high availability

## How It Works

1. All applications connect to ProxySQL (port 6033)
2. ProxySQL distributes queries across the Galera Cluster nodes
3. Galera Cluster ensures data consistency across all nodes
4. If a node fails, ProxySQL automatically routes traffic to healthy nodes

## Running the System

To start the system with Galera Cluster:

```bash
docker-compose -f docker-compose-galera.yml up -d
```

The first startup might take longer as the Galera Cluster initializes. Monitor the logs to ensure everything starts correctly:

```bash
docker-compose -f docker-compose-galera.yml logs -f
```

## Verifying the Setup

1. Check the ProxySQL admin interface:

```bash
mysql -h 127.0.0.1 -P 6032 -u admin -padmin
```

2. View the status of the Galera nodes:

```sql
SELECT * FROM mysql_servers;
```

3. Connect to any Galera node directly to verify replication:

```bash
mysql -h 127.0.0.1 -P 3309 -u root -proot
```

```sql
SHOW STATUS LIKE 'wsrep%';
```

## Benefits of This Configuration

- **High Availability**: System continues to operate even if a database node fails
- **Scalability**: Handles more concurrent users during peak enrollment periods
- **Zero Downtime Maintenance**: Nodes can be updated one at a time without downtime
- **Consistent Performance**: Load is balanced across all database nodes

## Monitoring

Monitor the health and performance of your Galera Cluster using:

```bash
docker-compose -f docker-compose-galera.yml exec galera-node1 mysql -u root -proot -e "SHOW GLOBAL STATUS LIKE 'wsrep_%';"
```

## Troubleshooting

If you encounter issues:

1. Check Galera cluster status:
```bash
docker-compose -f docker-compose-galera.yml exec galera-node1 mysql -u root -proot -e "SHOW STATUS LIKE 'wsrep_cluster_%';"
```

2. View ProxySQL logs:
```bash
docker-compose -f docker-compose-galera.yml logs proxysql
```

3. If a node falls out of sync, you may need to restart it:
```bash
docker-compose -f docker-compose-galera.yml restart galera-node2
```
