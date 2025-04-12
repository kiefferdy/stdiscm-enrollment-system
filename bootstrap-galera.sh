#!/bin/bash
# Script to bootstrap Galera

set -e

echo "Stopping all containers..."
docker-compose -f docker-compose-galera.yml down

# Create a temporary bootstrap compose file
echo "Creating temporary bootstrap compose file..."
cat > docker-compose-temp-bootstrap.yml << EOF
services:
  galera-node1:
    image: bitnami/mariadb-galera:latest
    container_name: galera-node1
    ports:
      - "3309:3306"
    environment:
      - MARIADB_GALERA_CLUSTER_NAME=enrollment_cluster
      - MARIADB_GALERA_CLUSTER_ADDRESS=gcomm://
      - MARIADB_ROOT_PASSWORD=root
      - MARIADB_DATABASE=enrollment
      - MARIADB_ENABLE_ROOT_FROM_ALL=yes
      - MARIADB_GALERA_MARIABACKUP_PASSWORD=backup_password
    volumes:
      - galera-node1-data:/bitnami/mariadb
      - ./galera-init:/docker-entrypoint-initdb.d
    networks:
      - enrollment-network

networks:
  enrollment-network:
    driver: bridge

volumes:
  galera-node1-data:
EOF

echo "Starting bootstrap node..."
docker-compose -f docker-compose-temp-bootstrap.yml up -d

echo "Waiting for galera-node1 to bootstrap (this may take 30-60 seconds)..."
sleep 30

# Check if bootstrapped
echo "Checking bootstrap status..."
if docker exec galera-node1 mariadb-admin -u root -proot ping &>/dev/null; then
  echo "Bootstrap successful! Stopping bootstrap container..."
  docker-compose -f docker-compose-temp-bootstrap.yml down
  
  echo "Starting full cluster..."
  docker-compose -f docker-compose-galera.yml up -d
  
  echo "Waiting for full cluster to form..."
  sleep 30
  docker exec galera-node1 mariadb -u root -proot -e "SHOW STATUS LIKE 'wsrep_cluster_size';" || echo "Could not check cluster size yet. Wait a bit longer and check manually."
  echo "Bootstrap complete!"
else
  echo "Bootstrap failed. Cleaning up..."
  docker-compose -f docker-compose-temp-bootstrap.yml down
  exit 1
fi

# Clean up
rm docker-compose-temp-bootstrap.yml
