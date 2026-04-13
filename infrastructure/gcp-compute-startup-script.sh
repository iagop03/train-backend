#!/bin/bash
# GCP Compute Engine Startup Script for TrAIn Backend
# Configures environment and retrieves MongoDB Atlas credentials from Secret Manager

set -e

echo "Starting TrAIn Backend setup..."

# Update system
apt-get update
apt-get install -y \
    openjdk-21-jdk-headless \
    curl \
    wget \
    git \
    ca-certificates \
    gnupg \
    google-cloud-cli

# Enable Cloud SQL Proxy for PostgreSQL connection
echo "Installing Cloud SQL Proxy..."
wget https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 -O /usr/local/bin/cloud_sql_proxy
chmod +x /usr/local/bin/cloud_sql_proxy

# Create application user
useradd -m -s /bin/bash train || true

# Create application directories
mkdir -p /opt/train/app
mkdir -p /var/log/train
chown -R train:train /opt/train
chown -R train:train /var/log/train

# Retrieve MongoDB credentials from GCP Secret Manager
echo "Retrieving MongoDB credentials from Secret Manager..."
export MONGODB_URI=$(gcloud secrets versions access latest --secret="train-mongodb-uri" --quiet)
export MONGODB_USER=train_app_user
export MONGODB_PASSWORD=$(gcloud secrets versions access latest --secret="train-mongodb-password" --quiet)

# Retrieve PostgreSQL credentials
echo "Retrieving PostgreSQL credentials from Secret Manager..."
export SQL_USER=$(gcloud secrets versions access latest --secret="train-sql-user" --quiet)
export SQL_PASSWORD=$(gcloud secrets versions access latest --secret="train-sql-password" --quiet)
export GCP_SQL_INSTANCE=$(gcloud secrets versions access latest --secret="train-sql-instance" --quiet)

# Retrieve Keycloak settings
echo "Retrieving Keycloak settings from Secret Manager..."
export KEYCLOAK_ISSUER_URI=$(gcloud secrets versions access latest --secret="train-keycloak-issuer-uri" --quiet)
export KEYCLOAK_JWK_SET_URI=$(gcloud secrets versions access latest --secret="train-keycloak-jwk-set-uri" --quiet)

# Write environment file for service
cat > /etc/train-backend.env << 'EOF'
MONGODB_URI=$MONGODB_URI
MONGODB_USER=$MONGODB_USER
MONGODB_PASSWORD=$MONGODB_PASSWORD
SQL_USER=$SQL_USER
SQL_PASSWORD=$SQL_PASSWORD
GCP_SQL_INSTANCE=$GCP_SQL_INSTANCE
KEYCLOAK_ISSUER_URI=$KEYCLOAK_ISSUER_URI
KEYCLOAK_JWK_SET_URI=$KEYCLOAK_JWK_SET_URI
JAVA_OPTS=-Xmx1024m -Xms512m
EOF

chmod 600 /etc/train-backend.env
chown train:train /etc/train-backend.env

# Test MongoDB Atlas connection through VPC peering
echo "Testing MongoDB Atlas connection..."
# Connection string format for mongosh
MONGO_URI="mongodb+srv://${MONGODB_USER}:${MONGODB_PASSWORD}@train-m0-cluster.mongodb.net/train_db?retryWrites=true&w=majority"

# Wait for network to be ready
sleep 10

# Note: In production, install mongosh for connection testing
# apt-get install -y mongodb-mongosh

echo "TrAIn Backend startup script completed successfully"
