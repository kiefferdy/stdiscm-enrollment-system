# Galera Cluster with ProxySQL Setup Guide

This document provides instructions for running the Enrollment System with Galera Cluster for database redundancy and ProxySQL for connection management.

## Architecture Overview

The database tier consists of:

1.  **3-Node Galera Cluster**: True multi-master replication using MariaDB Galera.
2.  **ProxySQL**: MySQL-aware load balancer that routes queries intelligently across the cluster.
3.  **Application Services**: Connect to the database through ProxySQL.

```
Application Services → ProxySQL → Galera Cluster (3 nodes)
```

## Components

### Galera Cluster

- **galera-node1**: First node, used for bootstrapping the cluster initially.
- **galera-node2**: Second node, joins the cluster.
- **galera-node3**: Third node, joins the cluster.

All nodes contain identical data due to synchronous replication. Defined in `docker-compose-galera.yml`.

### ProxySQL

- **proxysql**: Routes application queries to healthy Galera nodes. Defined in `docker-compose-galera.yml`. Configuration is managed via `proxysql/proxysql.cnf` and potentially `proxysql/init.sql`.

## Running the System

### Initial Setup (First Time Only)

**Important:** Before the first run, ensure any previous cluster data is cleaned up (see Reset section if needed).

1.  **Bootstrap the Cluster:**
    ```bash
    # Make the script executable if needed
    chmod +x bootstrap-galera.sh

    # Run the bootstrap script
    ./bootstrap-galera.sh
    ```
    This script handles the special procedure of starting the first node, letting it initialize, stopping it, and then starting the full cluster using `docker-compose-galera.yml`. It also runs the database initialization scripts found in `galera-init/`.

2.  **Monitor Logs:** Check the logs to ensure all services start correctly.
    ```bash
    docker-compose -f docker-compose-galera.yml logs -f
    ```

### Normal Startup (After Initial Setup)

If the cluster has been bootstrapped successfully before and you just need to start the services:

```bash
docker-compose -f docker-compose-galera.yml up -d
```

## Verifying the Setup

Use the test script:

```bash
# Make the script executable if needed
chmod +x test-galera.sh

# Run the test script
./test-galera.sh
```

This script checks:
- Connectivity and cluster size on each Galera node (`mariadb` client).
- Connectivity to ProxySQL (`mysql` client).

Alternatively, run manual checks:

### 1. Check Galera Cluster Status

```bash
# Check from any node, e.g., galera-node1
docker exec -it galera-node1 mariadb -uroot -proot -e "SHOW STATUS LIKE 'wsrep_cluster_size';"
# Expected output should show a size of 3 eventually.

docker exec -it galera-node1 mariadb -uroot -proot -e "SHOW STATUS LIKE 'wsrep_local_state_comment';"
# Expected output should be 'Synced' or 'Donor/Desynced' briefly during startup.
```

### 2. Check ProxySQL Backend Nodes

```bash
# Connect to ProxySQL Admin interface
docker exec -it proxysql mysql -h127.0.0.1 -P6032 -uadmin -padmin -e "SELECT hostgroup_id, hostname, port, status FROM runtime_mysql_servers;"
```
This should show all three Galera nodes (`galera-node1`, `galera-node2`, `galera-node3`) listed, ideally with `ONLINE` status.

### 3. Test Connection Through ProxySQL

```bash
# Connect via ProxySQL client port
docker exec -it proxysql mysql -h127.0.0.1 -P6033 -uroot -proot -e "SELECT @@hostname;"
```
Running this multiple times might show different underlying Galera node hostnames.

## Cluster Management Scripts

### Recovering a Failed Node

If a single Galera node container stops or fails, use the recovery script:

```bash
# Make the script executable if needed
chmod +x recover-galera.sh

# Run the recovery script
./recover-galera.sh
```
The script will prompt for the node name (e.g., `galera-node1`) and attempt to stop and restart it correctly using `docker-compose-galera.yml`.

### Resetting the Cluster (Development/Testing ONLY)

**WARNING:** This script deletes ALL data in the Galera cluster volumes.

```bash
# Make the script executable if needed
chmod +x reset-galera.sh

# Run the reset script (will ask for confirmation)
./reset-galera.sh
```
This script stops the cluster, removes the data volumes, and then runs `./bootstrap-galera.sh` to start fresh.

## Troubleshooting

1.  **Check Container Status:** `docker ps` (See which containers are running/restarting).
2.  **Check Galera Node Logs:** `docker logs galera-node1` (Replace with node name). Look for WSREP errors.
3.  **Check ProxySQL Logs:** `docker logs proxysql`. Look for connection errors to backend nodes.
4.  **Check Application Service Logs:** `docker logs auth-service` (Replace with service name). Look for database connection errors.
5.  **Run Test Script:** `./test-galera.sh` to check basic connectivity.
6.  **Restart a Service:** `docker-compose -f docker-compose-galera.yml restart <service_name>` (e.g., `proxysql`, `galera-node2`).
7.  **Full Reset:** If things are completely broken, use `./reset-galera.sh` (Data Loss!).
