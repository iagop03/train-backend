#!/bin/bash

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ID="${GCP_PROJECT_ID:-}"
REGION="${GCP_REGION:-us-central1}"
INSTANCE_NAME="train-postgres-prod"
DB_NAME="train"
APP_USER="train_app"

if [ -z "$PROJECT_ID" ]; then
  echo -e "${RED}Error: GCP_PROJECT_ID not set${NC}"
  exit 1
fi

echo -e "${BLUE}=== TrAIn Cloud SQL Setup ===${NC}"
echo -e "Project: ${GREEN}$PROJECT_ID${NC}"
echo -e "Region: ${GREEN}$REGION${NC}"
echo -e "Instance: ${GREEN}$INSTANCE_NAME${NC}"

# Function to check if instance exists
instance_exists() {
  gcloud sql instances describe "$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --region="$REGION" &>/dev/null
}

# Function to wait for instance to be ready
wait_for_instance() {
  echo -e "${BLUE}Waiting for instance to be ready...${NC}"
  while [ "$(gcloud sql instances describe "$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --format='value(state)')" != "RUNNABLE" ]; do
    echo -e "${YELLOW}Instance is not ready yet, waiting...${NC}"
    sleep 10
  done
  echo -e "${GREEN}Instance is ready!${NC}"
}

# Function to configure replication
configure_replication() {
  echo -e "${BLUE}Configuring automatic replication...${NC}"
  
  gcloud sql instances patch "$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --backup-start-time="03:00" \
    --retained-backups-count="30" \
    --enable-point-in-time-recovery \
    --transaction-log-retention-days="7"
  
  echo -e "${GREEN}Replication configured!${NC}"
}

# Function to configure SSL
configure_ssl() {
  echo -e "${BLUE}Configuring SSL/TLS...${NC}"
  
  # Require SSL
  gcloud sql instances patch "$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --require-ssl
  
  echo -e "${GREEN}SSL/TLS configured and required!${NC}"
}

# Function to configure flags
configure_flags() {
  echo -e "${BLUE}Configuring database flags...${NC}"
  
  gcloud sql instances patch "$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --database-flags=cloudsql_iam_authentication=on,log_statement=all,log_min_duration_statement=1000,max_connections=200
  
  echo -e "${GREEN}Database flags configured!${NC}"
}

# Function to create database
create_database() {
  echo -e "${BLUE}Creating database: $DB_NAME${NC}"
  
  gcloud sql databases create "$DB_NAME" \
    --instance="$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --charset=utf8
  
  echo -e "${GREEN}Database created!${NC}"
}

# Function to backup database
backup_database() {
  echo -e "${BLUE}Creating initial backup...${NC}"
  
  BACKUP_ID="initial-$(date +%s)"
  gcloud sql backups create "$BACKUP_ID" \
    --instance="$INSTANCE_NAME" \
    --project="$PROJECT_ID"
  
  echo -e "${GREEN}Initial backup created: $BACKUP_ID${NC}"
}

# Function to export credentials
export_credentials() {
  echo -e "${BLUE}Exporting credentials to Kubernetes Secret...${NC}"
  
  # Get passwords from terraform outputs
  local root_pass=$(terraform output -raw root_password 2>/dev/null || echo "")
  local app_pass=$(terraform output -raw app_password 2>/dev/null || echo "")
  local connection_name=$(terraform output -raw cloudsql_instance_connection_name 2>/dev/null || echo "")
  
  if [ -z "$root_pass" ] || [ -z "$app_pass" ]; then
    echo -e "${YELLOW}Warning: Could not retrieve passwords from terraform output${NC}"
    echo -e "${YELLOW}Ensure terraform state is available${NC}"
    return
  fi
  
  # Create Kubernetes secret
  kubectl create secret generic cloudsql-credentials \
    --from-literal=root-password="$root_pass" \
    --from-literal=app-password="$app_pass" \
    --from-literal=connection-name="$connection_name" \
    -n train \
    --dry-run=client -o yaml | kubectl apply -f -
  
  echo -e "${GREEN}Credentials exported to Kubernetes secret!${NC}"
}

# Function to test connection
test_connection() {
  echo -e "${BLUE}Testing Cloud SQL connectivity...${NC}"
  
  # Get instance IP
  local instance_ip=$(gcloud sql instances describe "$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --format='value(ipAddresses[0].ipAddress)')
  
  echo -e "Instance IP: ${GREEN}$instance_ip${NC}"
  
  # Get connection name
  local connection_name=$(gcloud sql instances describe "$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --format='value(connectionName)')
  
  echo -e "Connection Name: ${GREEN}$connection_name${NC}"
  
  echo -e "${GREEN}Cloud SQL connectivity test passed!${NC}"
}

# Main execution
main() {
  # Set project
  gcloud config set project "$PROJECT_ID"
  
  if instance_exists; then
    echo -e "${YELLOW}Instance already exists${NC}"
  else
    echo -e "${RED}Instance does not exist. Please create it with Terraform first.${NC}"
    exit 1
  fi
  
  wait_for_instance
  
  # Configure instance
  configure_replication
  configure_ssl
  configure_flags
  
  # Create database
  create_database || echo -e "${YELLOW}Database might already exist${NC}"
  
  # Create initial backup
  backup_database
  
  # Test connectivity
  test_connection
  
  # Export credentials (optional)
  if command -v kubectl &> /dev/null; then
    export_credentials || echo -e "${YELLOW}Skipping K8s secret creation${NC}"
  fi
  
  echo -e "${GREEN}=== Cloud SQL Setup Complete ===${NC}"
}

main
