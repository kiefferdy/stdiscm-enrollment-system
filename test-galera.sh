#!/bin/bash
# Script to test connection to Galera nodes

echo "Testing connection to Galera nodes..."

for node in galera-node1 galera-node2 galera-node3; do
  echo "Testing $node..."
  docker exec -it $node mariadb -uroot -proot -e "SHOW STATUS LIKE 'wsrep_cluster_size';" || echo "$node is not available"
done

echo "Testing ProxySQL..."
docker exec -it proxysql mysql -h127.0.0.1 -P6033 -uroot -proot -e "SELECT 'ProxySQL is working';" || echo "ProxySQL is not available"

echo "Test complete!"
