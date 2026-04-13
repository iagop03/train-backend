# MongoDB Atlas M0 Cluster Setup Guide

## Overview
This guide provides step-by-step instructions to set up MongoDB Atlas M0 cluster with VPC peering to GCP and IP whitelisting.

## Prerequisites
- MongoDB Atlas account with Organization
- GCP project with Compute Engine and SQL Admin APIs enabled
- Terraform >= 1.5
- MongoDB Atlas Provider >= 1.14
- Google Provider >= 5.0

## Step 1: Create MongoDB Atlas API Key

### In MongoDB Atlas:
1. Go to **Organization Settings** → **API Keys**
2. Click **Create API Key**
3. Set permissions:
   - Organization Project Creator
   - Organization Organization Member
4. Save the **Public Key** (API Key) and **Private Key**
5. Add IP whitelist for Terraform runner

## Step 2: Configure GCP Project

### Enable Required APIs:
```bash
gcloud services enable compute.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable secretmanager.googleapis.com
gcloud services enable servicenetworking.googleapis.com
```

### Create GCP Service Account (Optional, for CI/CD):
```bash
gcloud iam service-accounts create terraform-runner \
  --display-name="Terraform Runner"

gcloud projects add-iam-policy-binding PROJECT_ID \
  --member=serviceAccount:terraform-runner@PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/compute.networkAdmin

gcloud projects add-iam-policy-binding PROJECT_ID \
  --member=serviceAccount:terraform-runner@PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/cloudsql.admin

gcloud projects add-iam-policy-binding PROJECT_ID \
  --member=serviceAccount:terraform-runner@PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/secretmanager.admin
```

## Step 3: Prepare Terraform Variables

```bash
cd infrastructure
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your values:
```hcl
mongoodb_atlas_org_id = "ORGANIZATION_ID"
mongoodb_atlas_api_key = "PRIVATE_KEY_FROM_API_KEY"
gcp_project_id = "your-gcp-project"
gcp_region = "us-central1"
atlas_region = "US_CENTRAL_1"
```

## Step 4: Initialize and Plan Terraform

```bash
# Initialize Terraform
terraform init

# Validate configuration
terraform validate

# Plan infrastructure changes
terraform plan -out=tfplan
```

## Step 5: Apply Terraform Configuration

```bash
terraform apply tfplan
```

This will:
1. ✅ Create MongoDB Atlas Project
2. ✅ Create M0 Cluster with 3-node replica set
3. ✅ Create MongoDB Atlas VPC with CIDR 10.0.0.0/21
4. ✅ Create VPC peering between MongoDB Atlas and GCP
5. ✅ Accept peering on GCP side
6. ✅ Configure IP whitelisting for:
   - GCP Cloud SQL instance
   - GCP Compute Engine instances
7. ✅ Create MongoDB database user
8. ✅ Store credentials in GCP Secret Manager

## Step 6: Verify VPC Peering Connection

### Check from MongoDB Atlas:
```bash
# List peering connections
terraform output vpc_peering_status
```

Should show: **ACTIVE**

### Check from GCP:
```bash
gcloud compute networks peerings list \
  --project=YOUR_PROJECT_ID

# Output should show: train-mongodb-atlas-peering with status ACTIVE
```

## Step 7: Verify Network Connectivity

### From GCP Compute Engine Instance:
```bash
# SSH into Compute Engine instance
gcloud compute ssh INSTANCE_NAME

# Test connectivity to MongoDB Atlas
nc -zv <MONGODB_ATLAS_HOST> 27017

# Test with MongoDB client
mongosh "mongodb+srv://train_app_user:PASSWORD@CLUSTER.mongodb.net/train_db?retryWrites=true&w=majority"
```

## Step 8: Configure Application

### Update Spring Boot application.yml:
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}  # Retrieved from Secret Manager
      username: ${MONGODB_USER}
      password: ${MONGODB_PASSWORD}
      database: train_db
      auto-index-creation: true
```

### In GCP Compute Engine, set environment variables:
```bash
export MONGODB_URI=$(gcloud secrets versions access latest --secret="train-mongodb-uri")
export MONGODB_USER=train_app_user
export MONGODB_PASSWORD=$(gcloud secrets versions access latest --secret="train-mongodb-password")
```

## Step 9: Configure IP Whitelisting Additional IPs

If you need to add more IPs (e.g., development machines):

```bash
terraform apply -var='additional_whitelist_ips=["YOUR_IP/32"]'
```

Or manually in MongoDB Atlas:
1. Go to **Network Access** → **IP Access List**
2. Click **Add IP Address**
3. Enter IP or CIDR range
4. Add description
5. Confirm

## Step 10: Monitor and Maintain

### Check MongoDB Atlas cluster status:
```bash
terraform show | grep "cluster_state_name"
```

### Monitor VPC peering traffic:
```bash
gcloud compute networks peerings describe train-mongodb-atlas-peering \
  --network=default
```

### View MongoDB Atlas metrics:
1. Go to **Clusters** → **Metrics**
2. Monitor:
   - Network In/Out
   - Operations per second
   - Connections
   - CPU usage

## M0 Cluster Limitations

- **Storage**: 512 MB
- **Connections**: Limited to 100 concurrent
- **Backup**: Snapshot-based only (no continuous backup)
- **Encryption**: At-rest encryption not supported
- **Custom encryption key**: Not available
- **Point-in-time recovery**: Not available

## M0 to M2+ Upgrade Path

When you need more capacity:

```bash
# Update provider_instance_size_name in mongodb-atlas-config.tf
resource "mongodbatlas_cluster" "train_cluster" {
  ...
  provider_instance_size_name = "M2"  # or M5, M10, etc.
  ...
}

# Apply changes
terraform apply
```

## Troubleshooting

### VPC Peering Status: INITIATING
- Wait 5-10 minutes for automatic acceptance
- Ensure GCP network exists
- Check Google provider credentials

### Connection Timeouts
- Verify IP whitelist entries
- Check VPC peering status (should be ACTIVE)
- Ensure security groups allow traffic on port 27017
- Test with `nc -zv` from GCP instance

### Authentication Failures
- Verify database user credentials in Secret Manager
- Check username and password encoding (URL-safe format)
- Ensure user has appropriate role in admin database

### MongoDB Performance Issues on M0
- M0 clusters are development/test only
- Consider upgrading for production workloads
- Monitor active connections (max 100)
- Optimize queries to reduce working set size

## Security Best Practices

1. **API Keys**: Store MongoDB API keys in GCP Secret Manager
2. **Network**: Use VPC peering instead of public IP access
3. **Authentication**: Enable IP access lists and consider VPC peering-only mode
4. **Encryption**: Enable database-level encryption with customer-managed keys (M5+)
5. **Backups**: Enable automated snapshots
6. **Audit**: Enable MongoDB Atlas Audit Log

## Cleanup

To destroy all resources:

```bash
terraform destroy
```

This will:
- Delete MongoDB Atlas cluster
- Remove VPC peering connections
- Delete database users
- Remove GCP resources

**Warning**: This will delete all data in the MongoDB cluster!

## References

- [MongoDB Atlas API Documentation](https://docs.atlas.mongodb.com/api/)
- [MongoDB Atlas Terraform Provider](https://registry.terraform.io/providers/mongodb/mongodbatlas/latest/docs)
- [GCP VPC Peering Guide](https://cloud.google.com/vpc/docs/vpc-peering)
- [MongoDB Connection String URI Format](https://docs.mongodb.com/manual/reference/connection-string/)
