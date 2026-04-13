#!/bin/bash
# IAM Configuration Script for TrAIn GCP Project
# Sets up service accounts and IAM roles

set -e

PROJECT_ID="train-ai-gym-tracker"
REGION="us-central1"

# Service account names
APP_SA_NAME="train-app-service"
CLOUD_BUILD_SA_NAME="train-cloud-build"
CLOUD_RUN_SA_NAME="train-cloud-run"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}IAM Configuration for TrAIn${NC}"
echo -e "${GREEN}========================================${NC}"

# Create service accounts
log_info "Creating service accounts..."

log_info "Creating Cloud Run service account"
gcloud iam service-accounts create $CLOUD_RUN_SA_NAME \
    --display-name="Cloud Run Service Account for TrAIn" \
    --project=$PROJECT_ID || log_info "Service account already exists"

log_info "Creating Cloud Build service account"
gcloud iam service-accounts create $CLOUD_BUILD_SA_NAME \
    --display-name="Cloud Build Service Account for TrAIn" \
    --project=$PROJECT_ID || log_info "Service account already exists"

log_info "Creating application service account"
gcloud iam service-accounts create $APP_SA_NAME \
    --display-name="Application Service Account for TrAIn" \
    --project=$PROJECT_ID || log_info "Service account already exists"

echo -e "\n${YELLOW}Assigning IAM roles to Cloud Run SA${NC}"

# Cloud Run service account roles
log_info "Granting roles to Cloud Run service account..."

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_RUN_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/cloudsql.client \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_RUN_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/secretmanager.secretAccessor \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_RUN_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/logging.logWriter \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_RUN_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/monitoring.metricWriter \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_RUN_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/storage.objectViewer \
    --quiet

echo -e "\n${YELLOW}Assigning IAM roles to Cloud Build SA${NC}"

# Cloud Build service account roles
log_info "Granting roles to Cloud Build service account..."

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_BUILD_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/run.admin \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_BUILD_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/iam.serviceAccountUser \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_BUILD_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/storage.admin \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${CLOUD_BUILD_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/secretmanager.secretAccessor \
    --quiet

echo -e "\n${YELLOW}Assigning IAM roles to App SA${NC}"

# Application service account roles
log_info "Granting roles to application service account..."

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${APP_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/cloudsql.client \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${APP_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/secretmanager.secretAccessor \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:${APP_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com \
    --role=roles/storage.objectViewer \
    --quiet

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}IAM Configuration Complete!${NC}"
echo -e "${GREEN}========================================${NC}"

echo -e "\n${YELLOW}Service Accounts Created:${NC}"
echo "  - ${CLOUD_RUN_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
echo "  - ${CLOUD_BUILD_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
echo "  - ${APP_SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
