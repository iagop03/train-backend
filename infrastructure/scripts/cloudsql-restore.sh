#!/bin/bash

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-}"
INSTANCE_NAME="${CLOUDSQL_INSTANCE:-train-postgres-prod}"
BACKUP_ID="${1:-}"

if [ -z "$PROJECT_ID" ]; then
  echo -e "${RED}Error: GCP_PROJECT_ID not set${NC}"
  exit 1
fi

if [ -z "$BACKUP_ID" ]; then
  echo -e "${YELLOW}Available backups:${NC}"
  gcloud sql backups list \
    --instance="$INSTANCE_NAME" \
    --project="$PROJECT_ID" \
    --format='table(name,windowStartTime,status)'
  echo ""
  echo "Usage: $0 <backup-id>"
  exit 1
fi

echo -e "${BLUE}Restoring from backup: $BACKUP_ID${NC}"
echo -e "${YELLOW}This will restore the instance from the specified backup.${NC}"
read -p "Are you sure? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
  echo -e "${YELLOW}Restore cancelled${NC}"
  exit 1
fi

gcloud sql backups restore "$BACKUP_ID" \
  --backup-instance="$INSTANCE_NAME" \
  --project="$PROJECT_ID"

echo -e "${GREEN}Restore started successfully!${NC}"
