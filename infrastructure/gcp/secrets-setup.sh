#!/bin/bash
# Secret Manager Setup Script for TrAIn GCP Project
# Stores sensitive configuration in Google Cloud Secret Manager

set -e

PROJECT_ID="train-ai-gym-tracker"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Secret Manager Setup for TrAIn${NC}"
echo -e "${GREEN}========================================${NC}"

# Function to create or update secret
create_or_update_secret() {
    local secret_name=$1
    local secret_value=$2
    
    if gcloud secrets describe $secret_name --project=$PROJECT_ID &>/dev/null; then
        log_info "Updating secret: $secret_name"
        echo -n "$secret_value" | gcloud secrets versions add $secret_name \
            --data-file=- \
            --project=$PROJECT_ID
    else
        log_info "Creating secret: $secret_name"
        echo -n "$secret_value" | gcloud secrets create $secret_name \
            --data-file=- \
            --replication-policy="automatic" \
            --project=$PROJECT_ID
    fi
}

echo -e "\n${BLUE}Setting up secrets...${NC}"
echo -e "${YELLOW}Please provide the following values:${NC}\n"

# Database secrets
read -p "PostgreSQL Admin Password: " -s DB_ADMIN_PASSWORD
echo
read -p "PostgreSQL Database Name (default: train_db): " DB_NAME
DB_NAME=${DB_NAME:-train_db}

read -p "MongoDB Connection String (press Enter to skip): " MONGODB_URI

# Application secrets
read -p "JWT Secret Key: " -s JWT_SECRET
echo
read -p "Application API Key: " -s API_KEY
echo

# Keycloak secrets
read -p "Keycloak Admin Username (default: admin): " KEYCLOAK_USER
KEYCLOAK_USER=${KEYCLOAK_USER:-admin}
read -p "Keycloak Admin Password: " -s KEYCLOAK_PASSWORD
echo
read -p "Keycloak Realm (default: train): " KEYCLOAK_REALM
KEYCLOAK_REALM=${KEYCLOAK_REALM:-train}

# Mail secrets (optional)
read -p "Email Service API Key (optional, press Enter to skip): " EMAIL_API_KEY

# Create secrets
log_info "Creating secrets in Secret Manager..."

create_or_update_secret "db-admin-password" "$DB_ADMIN_PASSWORD"
create_or_update_secret "db-name" "$DB_NAME"
create_or_update_secret "jwt-secret" "$JWT_SECRET"
create_or_update_secret "api-key" "$API_KEY"
create_or_update_secret "keycloak-user" "$KEYCLOAK_USER"
create_or_update_secret "keycloak-password" "$KEYCLOAK_PASSWORD"
create_or_update_secret "keycloak-realm" "$KEYCLOAK_REALM"

if [ -n "$MONGODB_URI" ]; then
    create_or_update_secret "mongodb-uri" "$MONGODB_URI"
fi

if [ -n "$EMAIL_API_KEY" ]; then
    create_or_update_secret "email-api-key" "$EMAIL_API_KEY"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Secrets Created Successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

log_info "Listing all secrets:"
gcloud secrets list --project=$PROJECT_ID
