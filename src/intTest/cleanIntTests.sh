#!/bin/bash

# Script to clean up TestContainers after integration tests
# This can be run manually or added to a CI/CD pipeline

echo "Cleaning up TestContainers from integration tests..."

# Find and remove test containers
docker ps -a | grep 'testcontainers' | awk '{print $1}' | xargs -r docker rm -f

# Remove test container volumes
docker volume ls | grep 'testcontainers' | awk '{print $2}' | xargs -r docker volume rm

# Remove test container networks
docker network ls | grep 'testcontainers' | awk '{print $1}' | xargs -r docker network rm

echo "TestContainers cleanup complete!"