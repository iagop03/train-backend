#!/bin/bash

set -euo pipefail

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-}"
INSTANCE_NAME="${CLOUDSQL_INSTANCE:-train-postgres-prod}"

if [ -z "$PROJECT_ID" ]; then
  echo "Error: GCP_PROJECT_ID not set"
  exit 1
fi

echo -e "${BLUE}Creating on-demand backup...${NC}"

BACKUP_ID="backup-$(date +%Y%m%d-%H%M%S)"

gcloud sql backups create "$BACKUP_ID" \
  --instance="$INSTANCE_NAME" \
  --project="$PROJECT_ID" \
  --async

echo -e "${GREEN}Backup initiated: $BACKUP_ID${NC}"

# List recent backups
echo -e "${BLUE}Recent backups:${NC}"
gcloud sql backups list \
  --instance="$INSTANCE_NAME" \
  --project="$PROJECT_ID" \
  --limit=10 \
  --format='table(name,windowStartTime,status)'
