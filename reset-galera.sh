#!/bin/bash
# WARNING: This script removes ALL Galera cluster data and starts fresh

set -e

echo "WARNING: This will delete ALL data in the Galera cluster."
echo "Type 'yes' to confirm or anything else to cancel:"
read confirm

if [ "$confirm" != "yes" ]; then
  echo "Operation cancelled."
  exit 0
fi

echo "Stopping all containers..."
docker-compose -f docker-compose-galera.yml down

echo "Removing Galera volumes..."
docker volume rm stdiscm-enrollment-system_galera-node1-data || true
docker volume rm stdiscm-enrollment-system_galera-node2-data || true
docker volume rm stdiscm-enrollment-system_galera-node3-data || true

echo "Now running bootstrap script..."
bash ./bootstrap-galera.sh

echo "Reset complete!"
