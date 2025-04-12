#!/bin/bash
# Script to help recover a Galera node after failure

set -e

# Function to check if node is already part of a cluster
check_cluster_status() {
  local node=$1
  echo "Checking status of $node..."
  docker exec -it $node mariadb -uroot -proot -e "SHOW STATUS LIKE 'wsrep_cluster_size'" || return 1
}

# Function to check which nodes are running
check_running_nodes() {
  echo "Checking which Galera nodes are running..."
  docker ps | grep galera-node
}

# Function to recover a node by forcing it to join existing cluster
recover_node() {
  local node=$1
  echo "Recovering $node..."
  
  # Step 1: Stop the node
  echo "Stopping $node..."
  docker-compose -f docker-compose-galera.yml stop $node
  
  # Step 2: Start the node with connection to other nodes
  echo "Starting $node..."
  docker-compose -f docker-compose-galera.yml up -d $node
  
  # Step 3: Check status
  echo "Waiting for node to come online..."
  sleep 20
  check_cluster_status $node
}

# Main script
echo "=== Galera Cluster Recovery Tool ==="
check_running_nodes

# Ask which node to recover
echo ""
echo "Which node would you like to recover? (e.g., galera-node1)"
read node_to_recover

# Recover the specified node
recover_node $node_to_recover

echo ""
echo "Recovery process completed. Check logs with: docker logs $node_to_recover"
