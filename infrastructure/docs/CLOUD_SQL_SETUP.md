# Cloud SQL PostgreSQL Setup - TrAIn Project

## Overview
This document provides comprehensive instructions for setting up Cloud SQL PostgreSQL with VPC private networking, SSL, automatic backups, and monitoring.

## Architecture

```
┌─────────────────────────────────────────────┐
│         GCP Project                         │
├─────────────────────────────────────────────┤
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │  VPC Network (train-cloudsql-vpc)   │  │
│  │                                      │  │
│  │  ┌────────────────────────────────┐ │  │
│  │  │ Subnet (10.1.0.0/24)          │ │  │
│  │  │                                │ │  │
│  │  │  Cloud SQL Instance           │ │  │
│  │  │  ├─ PostgreSQL 15             │ │  │
│  │  │  ├─ Private IP Only           │ │  │
│  │  │  ├─ High Availability         │ │  │
│  │  │  └─ SSD Storage               │ │  │
│  │  └────────────────────────────────┘ │  │
│  │                                      │  │
│  │  Private Service Connection         │  │
│  │  (servicenetworking.googleapis.com) │  │
│  └──────────────────────────────────────┘  │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │  Backup & Monitoring                │  │
│  │  ├─ Cloud Storage (Backups)          │  │
│  │  ├─ Cloud Monitoring                 │  │
│  │  ├─ Cloud Logging                    │  │
│  │  └─ Query Insights                   │  │
│  └──────────────────────────────────────┘  │
│                                             │
└─────────────────────────────────────────────┘
```

## Prerequisites

1. GCP Project with billing enabled
2. Terraform >= 1.0
3. Google Cloud SDK (gcloud) installed and configured
4. Appropriate IAM roles:
   - `roles/cloudsql.admin`
   - `roles/compute.networkAdmin`
   - `roles/iam.serviceAccountAdmin`
   - `roles/storage.admin`

## Step-by-Step Setup

### 1. Initialize Terraform

```bash
cd infrastructure/terraform

# Create terraform.tfvars
cp terraform.tfvars.example terraform.tfvars

# Edit with your values
vim terraform.tfvars

# Initialize Terraform
terraform init

# Validate configuration
terraform validate

# Plan deployment
terraform plan -out=tfplan
```

### 2. Deploy Infrastructure

```bash
# Apply Terraform configuration
terraform apply tfplan

# Save outputs
terraform output -json > outputs.json
```

### 3. Configure Database

```bash
# Get Cloud SQL connection details
CONNECTION_NAME=$(terraform output -raw cloudsql_connection_name)
PRIVATE_IP=$(terraform output -raw cloudsql_private_ip)

echo "Instance: $CONNECTION_NAME"
echo "Private IP: $PRIVATE_IP"

# Initialize database schema
gcloud sql connect train-postgres-prod \
    --project=YOUR_PROJECT_ID \
    --user=postgres < infrastructure/scripts/init-database.sql
```

### 4. Download SSL Certificates

```bash
# Create certificates directory
mkdir -p /path/to/certs
cd /path/to/certs

# Download server CA certificate
gcloud sql ssl-certs describe server-ca-cert \
    --instance=train-postgres-prod \
    --project=YOUR_PROJECT_ID \
    --format='get(cert)' > server-ca.pem

# Create client certificate
gcloud sql ssl-certs create client-cert \
    --instance=train-postgres-prod \
    --project=YOUR_PROJECT_ID

# Download client certificate and key
gcloud sql ssl-certs describe client-cert \
    --instance=train-postgres-prod \
    --project=YOUR_PROJECT_ID \
    --format='get(cert)' > client-cert.pem

# Set proper permissions
chmod 600 *.pem
```

### 5. Configure Application Connection

#### For Spring Boot Application

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://PRIVATE_IP:5432/train_db
    username: train_app
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL15Dialect
        jdbc:
          batch_size: 20
          fetch_size: 50
```

#### Using Cloud SQL Proxy

```bash
# Download Cloud SQL Proxy
wget https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 -O cloud_sql_proxy
chmod +x cloud_sql_proxy

# Start proxy
./cloud_sql_proxy \
    -instances=PROJECT_ID:us-central1:train-postgres-prod=tcp:5432 \
    -credential_file=/path/to/service-account-key.json

# In application, connect to localhost:5432
```

#### Environment Variables

```bash
export DB_HOST="10.1.0.0"  # Cloud SQL private IP
export DB_PORT="5432"
export DB_NAME="train_db"
export DB_USER="train_app"
export DB_PASSWORD="<password>"

# For SSL connections
export DB_SSL_MODE="require"
export DB_SSL_CERT="/path/to/certs/client-cert.pem"
export DB_SSL_KEY="/path/to/certs/client-key.pem"
export DB_SSL_ROOT_CERT="/path/to/certs/server-ca.pem"
```

### 6. User Management

#### Root User (Postgres)
```sql
-- Change password
ALTER USER postgres WITH PASSWORD 'new_strong_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE train_db TO postgres;
```

#### Application User
```sql
-- Create application-specific user (if not exists)
CREATE USER train_app WITH PASSWORD 'app_strong_password';

-- Grant schema permissions
GRANT USAGE ON SCHEMA train_schema TO train_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA train_schema TO train_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA train_schema TO train_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA train_schema TO train_app;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA train_schema GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO train_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA train_schema GRANT USAGE, SELECT ON SEQUENCES TO train_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA train_schema GRANT EXECUTE ON FUNCTIONS TO train_app;
```

#### Read-Only User (Analytics)
```sql
CREATE USER train_analytics WITH PASSWORD 'analytics_strong_password';

GRANT CONNECT ON DATABASE train_db TO train_analytics;
GRANT USAGE ON SCHEMA train_schema TO train_analytics;
GRANT SELECT ON ALL TABLES IN SCHEMA train_schema TO train_analytics;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA train_schema TO train_analytics;

ALTER DEFAULT PRIVILEGES IN SCHEMA train_schema GRANT SELECT ON TABLES TO train_analytics;
```

### 7. Backup Management

#### Automatic Backups
Automatically configured with:
- Daily backups at 03:00 UTC
- 7-day transaction log retention
- 30 backups retained
- Point-in-time recovery enabled

#### Manual Backup
```bash
GCP_PROJECT_ID=YOUR_PROJECT_ID ./infrastructure/scripts/backup-restore.sh create "Pre-deployment backup"
```

#### List Backups
```bash
GCP_PROJECT_ID=YOUR_PROJECT_ID ./infrastructure/scripts/backup-restore.sh list
```

#### Export Database
```bash
GCP_PROJECT_ID=YOUR_PROJECT_ID ./infrastructure/scripts/backup-restore.sh export train_db_backup.sql
```

#### Import Database
```bash
GCP_PROJECT_ID=YOUR_PROJECT_ID ./infrastructure/scripts/backup-restore.sh import train_db_backup.sql
```

#### Point-in-Time Recovery
```bash
GCP_PROJECT_ID=YOUR_PROJECT_ID ./infrastructure/scripts/backup-restore.sh pitr "2024-01-15T14:30:00" train-postgres-restore
```

### 8. Monitoring & Alerting

#### Cloud Monitoring Dashboards
```bash
# Create dashboard
gcloud monitoring dashboards create \
    --config-from-file=infrastructure/monitoring/dashboard.json
```

#### Key Metrics to Monitor
- CPU Utilization (alert > 80%)
- Disk Utilization (alert > 85%)
- Database Connections (alert > 150)
- Query Performance (Query Insights)
- Replication Lag (for HA setup)
- Backup Status

#### Query Insights
Enabled by default, provides:
- Slowest queries
- Most resource-consuming queries
- Query fingerprinting
- Execution metrics

### 9. Connection Testing

#### Test with psql
```bash
# Using Cloud SQL Proxy
psql -h localhost -U train_app -d train_db

# Using public IP (if configured)
psql -h PUBLIC_IP -U train_app -d train_db

# With SSL
psql "host=PRIVATE_IP user=train_app dbname=train_db \
     sslmode=require sslcert=/path/to/certs/client-cert.pem \
     sslkey=/path/to/certs/client-key.pem \
     sslrootcert=/path/to/certs/server-ca.pem"
```

#### Connection Verification Query
```sql
SELECT version();
SELECT current_user;
SELECT current_database();
SELECT current_timestamp AT TIME ZONE 'UTC';
```

## Security Best Practices

1. **Network Security**
   - ✓ Private IP only (no public IP)
   - ✓ VPC service networking
   - ✓ Firewall rules per application

2. **Authentication**
   - ✓ Strong passwords (32+ characters)
   - ✓ IAM authentication for service accounts
   - ✓ No passwords in code/git

3. **Encryption**
   - ✓ SSL/TLS for all connections
   - ✓ Database encryption at rest
   - ✓ Backup encryption with KMS

4. **Backups**
   - ✓ Automated daily backups
   - ✓ Cross-region backup replication (optional)
   - ✓ Regular restore testing

5. **Monitoring**
   - ✓ Query audit logging enabled
   - ✓ Connection logging enabled
   - ✓ Cloud Monitoring alerts configured

## Troubleshooting

### Connection Issues
```bash
# Check instance status
gcloud sql instances describe train-postgres-prod \
    --project=YOUR_PROJECT_ID

# Check authorized networks
gcloud sql instances describe train-postgres-prod \
    --project=YOUR_PROJECT_ID \
    --format='get(settings.ipConfiguration)'

# View recent operations
gcloud sql operations list \
    --instance=train-postgres-prod \
    --project=YOUR_PROJECT_ID
```

### Performance Issues
```sql
-- Check connections
SELECT count(*) FROM pg_stat_activity;

-- Slow queries
SELECT query, mean_exec_time FROM pg_stat_statements
ORDER BY mean_exec_time DESC LIMIT 10;

-- Database size
SELECT pg_size_pretty(pg_database_size(current_database()));

-- Table sizes
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Backup Issues
```bash
# Check backup status
gcloud sql backups describe BACKUP_ID \
    --instance=train-postgres-prod \
    --project=YOUR_PROJECT_ID

# View backup logs
gcloud logging read "resource.type=cloudsql_database" \
    --limit=50 --format=json
```

## Maintenance

### Monthly Tasks
- Review backup retention and storage costs
- Analyze slow query logs
- Verify replication lag (if HA enabled)
- Test restore procedures

### Quarterly Tasks
- Review and optimize indexes
- Update database parameters if needed
- Plan for storage growth
- Security audit

### Annual Tasks
- Major version upgrade planning
- Disaster recovery testing
- Performance baseline review

## Cost Optimization

1. **Compute**: db-custom-2-8192 = ~$100-150/month
2. **Storage**: 100GB SSD = ~$20/month
3. **Backups**: 30 daily backups = ~$10-15/month
4. **Network**: Private IP = free

**Total Estimated: ~$140-180/month**

## References
- [Cloud SQL Documentation](https://cloud.google.com/sql/docs)
- [Cloud SQL VPC Networking](https://cloud.google.com/sql/docs/postgres/private-ip)
- [PostgreSQL 15 Docs](https://www.postgresql.org/docs/15/)
- [Terraform Google Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)
