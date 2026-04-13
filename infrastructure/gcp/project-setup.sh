#!/bin/bash
# GCP Project Setup Script for TrAIn AI Gym Tracker
# This script configures GCP project and enables required APIs

set -e

# Configuration
PROJECT_ID="train-ai-gym-tracker"
PROJECT_NAME="TrAIn - AI Gym Tracker"
REGION="us-central1"
BILLING_ACCOUNT_ID="${BILLING_ACCOUNT_ID:-}" # Must be provided as env var

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}GCP Project Setup for TrAIn${NC}"
echo -e "${GREEN}========================================${NC}"

# Function to log messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if gcloud CLI is installed
if ! command -v gcloud &> /dev/null; then
    log_error "gcloud CLI is not installed. Please install it first."
    exit 1
fi

# Check if billing account is provided
if [ -z "$BILLING_ACCOUNT_ID" ]; then
    log_error "BILLING_ACCOUNT_ID environment variable is not set."
    log_info "Get billing account ID: gcloud billing accounts list"
    exit 1
fi

log_info "Creating GCP project: $PROJECT_ID"
gcloud projects create $PROJECT_ID \
    --name="$PROJECT_NAME" \
    --set-as-default

log_info "Linking billing account to project"
gcloud billing projects link $PROJECT_ID \
    --billing-account=$BILLING_ACCOUNT_ID

log_info "Enabling required APIs..."

# Core compute and container APIs
log_info "Enabling Cloud Run API"
gcloud services enable run.googleapis.com --project=$PROJECT_ID

log_info "Enabling Cloud Build API"
gcloud services enable cloudbuild.googleapis.com --project=$PROJECT_ID

log_info "Enabling Container Registry API"
gcloud services enable containerregistry.googleapis.com --project=$PROJECT_ID

log_info "Enabling Artifact Registry API"
gcloud services enable artifactregistry.googleapis.com --project=$PROJECT_ID

# Database APIs
log_info "Enabling Cloud SQL Admin API"
gcloud services enable sqladmin.googleapis.com --project=$PROJECT_ID

log_info "Enabling Cloud Firestore API"
gcloud services enable firestore.googleapis.com --project=$PROJECT_ID

# Security and secrets management
log_info "Enabling Secret Manager API"
gcloud services enable secretmanager.googleapis.com --project=$PROJECT_ID

log_info "Enabling Cloud Identity and Access Management API"
gcloud services enable iam.googleapis.com --project=$PROJECT_ID

# Monitoring and logging
log_info "Enabling Cloud Logging API"
gcloud services enable logging.googleapis.com --project=$PROJECT_ID

log_info "Enabling Cloud Monitoring API"
gcloud services enable monitoring.googleapis.com --project=$PROJECT_ID

# Storage
log_info "Enabling Cloud Storage API"
gcloud services enable storage-api.googleapis.com --project=$PROJECT_ID

log_info "Enabling Cloud Storage (GCS)"
gcloud services enable storage-component.googleapis.com --project=$PROJECT_ID

# Additional useful APIs
log_info "Enabling Cloud Resource Manager API"
gcloud services enable cloudresourcemanager.googleapis.com --project=$PROJECT_ID

log_info "Enabling Service Networking API"
gcloud services enable servicenetworking.googleapis.com --project=$PROJECT_ID

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}API Enablement Complete!${NC}"
echo -e "${GREEN}========================================${NC}"

# Display next steps
echo -e "\n${YELLOW}Next Steps:${NC}"
echo "1. Set default region and zone:"
echo "   gcloud config set compute/region $REGION"
echo "   gcloud config set compute/zone ${REGION}-a"
echo ""
echo "2. Create service accounts (see iam-setup.sh)"
echo ""
echo "3. Configure environment variables in .env.gcp"
echo ""
echo "4. Deploy infrastructure (see terraform or manual deployment guide)"
