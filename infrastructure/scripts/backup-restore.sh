#!/bin/bash

# Cloud SQL Backup and Restore Script
# Usage: ./backup-restore.sh <command> [options]

set -euo pipefail

# Configuration
PROJECT_ID="${GCP_PROJECT_ID:-}"
INSTANCE_NAME="train-postgres-prod"
REGION="us-central1"
BACKUP_BUCKET="train-cloudsql-backups-${PROJECT_ID}"
RETENTION_DAYS=30

if [ -z "$PROJECT_ID" ]; then
    echo "Error: GCP_PROJECT_ID environment variable not set"
    exit 1
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# List all backups
list_backups() {
    log_info "Listing backups for instance: $INSTANCE_NAME"
    gcloud sql backups list \
        --instance=$INSTANCE_NAME \
        --project=$PROJECT_ID \
        --sort-by=~windowStartTime
}

# Create manual backup
create_backup() {
    local description="${1:-Manual backup}"
    log_info "Creating backup: $description"
    
    gcloud sql backups create \
        --instance=$INSTANCE_NAME \
        --project=$PROJECT_ID \
        --description="$description"
    
    log_info "Backup created successfully"
}

# Export database to Cloud Storage
export_database() {
    local backup_file="${1:-train_db_$(date +%Y%m%d_%H%M%S).sql}"
    log_info "Exporting database to gs://$BACKUP_BUCKET/$backup_file"
    
    gcloud sql export sql $INSTANCE_NAME \
        gs://$BACKUP_BUCKET/$backup_file \
        --database=train_db \
        --project=$PROJECT_ID
    
    log_info "Database exported successfully"
}

# Import database from Cloud Storage
import_database() {
    local backup_file="${1:-}"
    
    if [ -z "$backup_file" ]; then
        log_error "Backup file path required for import"
        exit 1
    fi
    
    log_warn "Importing database from gs://$BACKUP_BUCKET/$backup_file"
    log_warn "This will overwrite existing data!"
    read -p "Are you sure? (yes/no): " confirm
    
    if [ "$confirm" != "yes" ]; then
        log_info "Import cancelled"
        exit 0
    fi
    
    gcloud sql import sql $INSTANCE_NAME \
        gs://$BACKUP_BUCKET/$backup_file \
        --database=train_db \
        --project=$PROJECT_ID
    
    log_info "Database imported successfully"
}

# Clone instance for testing
clone_instance() {
    local clone_name="${1:-train-postgres-clone-$(date +%s)}"
    log_info "Cloning instance to: $clone_name"
    
    local backup_id=$(gcloud sql backups list \
        --instance=$INSTANCE_NAME \
        --project=$PROJECT_ID \
        --sort-by=~windowStartTime \
        --limit=1 \
        --format='value(name)')
    
    if [ -z "$backup_id" ]; then
        log_error "No backup found to clone from"
        exit 1
    fi
    
    gcloud sql backups restore $backup_id \
        --backup-instance=$INSTANCE_NAME \
        --backup-project=$PROJECT_ID \
        --target-instance=$clone_name
    
    log_info "Instance cloned successfully"
}

# Get backup details
get_backup_details() {
    local backup_id="${1:-}"
    
    if [ -z "$backup_id" ]; then
        log_error "Backup ID required"
        exit 1
    fi
    
    log_info "Getting details for backup: $backup_id"
    gcloud sql backups describe $backup_id \
        --instance=$INSTANCE_NAME \
        --project=$PROJECT_ID
}

# Delete old backups
cleanup_old_backups() {
    log_info "Cleaning up backups older than $RETENTION_DAYS days"
    
    local cutoff_date=$(date -d "$RETENTION_DAYS days ago" +%Y-%m-%d 2>/dev/null || date -v-${RETENTION_DAYS}d +%Y-%m-%d)
    
    gcloud sql backups list \
        --instance=$INSTANCE_NAME \
        --project=$PROJECT_ID \
        --format='value(name,windowStartTime.date("%Y-%m-%d"))' | \
    while read backup_id backup_date; do
        if [[ $backup_date < $cutoff_date ]]; then
            log_warn "Deleting backup: $backup_id ($backup_date)"
            gcloud sql backups delete $backup_id \
                --instance=$INSTANCE_NAME \
                --project=$PROJECT_ID \
                --quiet
        fi
    done
    
    log_info "Cleanup completed"
}

# Point-in-time recovery
pitr_restore() {
    local timestamp="${1:-}"
    local target_instance="${2:-train-postgres-pitr}"
    
    if [ -z "$timestamp" ]; then
        log_error "Timestamp required (format: YYYY-MM-DDTHH:MM:SS)"
        exit 1
    fi
    
    log_warn "Restoring to point-in-time: $timestamp"
    log_warn "This will create a new instance: $target_instance"
    
    gcloud sql backups restore \
        --backup-instance=$INSTANCE_NAME \
        --backup-project=$PROJECT_ID \
        --backup-configuration=automatic \
        --point-in-time=$timestamp \
        --target-instance=$target_instance
    
    log_info "Point-in-time restore completed"
}

# Main command dispatcher
main() {
    local command="${1:-help}"
    
    case $command in
        list)
            list_backups
            ;;
        create)
            create_backup "${2:-Manual backup}"
            ;;
        export)
            export_database "${2:-}"
            ;;
        import)
            import_database "${2:-}"
            ;;
        clone)
            clone_instance "${2:-}"
            ;;
        details)
            get_backup_details "${2:-}"
            ;;
        cleanup)
            cleanup_old_backups
            ;;
        pitr)
            pitr_restore "${2:-}" "${3:-}"
            ;;
        help|*)
            cat << EOF
Cloud SQL Backup and Restore Script

Usage: $0 <command> [options]

Commands:
    list                    List all backups
    create [description]    Create manual backup
    export [filename]       Export database to Cloud Storage
    import <filename>       Import database from Cloud Storage
    clone [name]           Clone instance from latest backup
    details <backup_id>    Get backup details
    cleanup                 Delete backups older than $RETENTION_DAYS days
    pitr <timestamp> [name] Point-in-time recovery
    help                   Show this help message

Environment Variables:
    GCP_PROJECT_ID         GCP Project ID (required)

Examples:
    GCP_PROJECT_ID=my-project $0 list
    GCP_PROJECT_ID=my-project $0 create "Pre-deployment backup"
    GCP_PROJECT_ID=my-project $0 export train_db_backup.sql
    GCP_PROJECT_ID=my-project $0 pitr "2024-01-15T14:30:00"
EOF
            ;;
    esac
}

main "$@"
