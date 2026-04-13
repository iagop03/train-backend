# TrAIn GCP Setup Guide

This guide provides step-by-step instructions to set up the Google Cloud Platform infrastructure for the TrAIn AI Gym Tracker project.

## Prerequisites

1. **Google Cloud Account**: Create a free or paid Google Cloud account
2. **gcloud CLI**: Install and configure the [Google Cloud CLI](https://cloud.google.com/sdk/docs/install)
3. **Terraform** (optional): For Infrastructure as Code deployment
4. **Git**: For cloning repositories
5. **Docker**: For local testing

## Setup Steps

### 1. Initial Configuration

```bash
# Login to Google Cloud
gcloud auth login

# Set your project ID
export PROJECT_ID="train-ai-gym-tracker"
gcloud config set project $PROJECT_ID

# Set default region
gcloud config set compute/region us-central1
gcloud config set compute/zone us-central1-a
```

### 2. Enable Required APIs

Run the project setup script:

```bash
cd infrastructure/gcp
chmod +x project-setup.sh

# Set billing account ID
export BILLING_ACCOUNT_ID="YOUR_BILLING_ACCOUNT_ID"
./project-setup.sh
```

**Get your Billing Account ID:**
```bash
gcloud billing accounts list
```

### 3. Configure IAM and Service Accounts

```bash
chmod +x iam-setup.sh
./iam-setup.sh
```

This script will:
- Create service accounts for Cloud Run and Cloud Build
- Assign necessary IAM roles
- Set up security permissions

### 4. Set Up Secrets

```bash
chmod +x secrets-setup.sh
./secrets-setup.sh
```

You will be prompted to enter:
- PostgreSQL admin password
- JWT secret key
- API keys
- Keycloak credentials
- MongoDB connection string (optional)
- Email service credentials (optional)

### 5. Infrastructure Deployment (Using Terraform)

**Option A: Using Terraform**

```bash
cd terraform

# Initialize Terraform
terraform init

# Review planned changes
terraform plan -out=tfplan

# Apply configuration
terraform apply tfplan

# Save outputs
terraform output -json > outputs.json
```

**Option B: Manual Deployment**

If using manual deployment, follow the GCP Console steps in the sections below.

### 6. Create Cloud SQL Instance (Manual)

```bash
GCP_INSTANCE_NAME="train-postgres-primary"
GCP_REGION="us-central1"

gcloud sql instances create $GCP_INSTANCE_NAME \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=$GCP_REGION \
  --network=train-vpc \
  --backup \
  --enable-bin-log \
  --retained-backups-count=30 \
  --transaction-log-retention-days=7

# Create database
gcloud sql databases create train_db \
  --instance=$GCP_INSTANCE_NAME

# Create application user
gcloud sql users create app_user \
  --instance=$GCP_INSTANCE_NAME \
  --password=$(openssl rand -base64 32)
```

### 7. Deploy Cloud Run Service

```bash
# Build and push Docker image
docker build -t gcr.io/$PROJECT_ID/train-backend:latest .
gcloud auth configure-docker
docker push gcr.io/$PROJECT_ID/train-backend:latest

# Deploy to Cloud Run
gcloud run deploy train-backend \
  --image=gcr.io/$PROJECT_ID/train-backend:latest \
  --region=us-central1 \
  --service-account=train-cloud-run@$PROJECT_ID.iam.gserviceaccount.com \
  --allow-unauthenticated \
  --memory=2Gi \
  --cpu=2 \
  --timeout=3600 \
  --set-cloudsql-instances=train-ai-gym-tracker:us-central1:train-postgres-primary
```

### 8. Configure Cloud Build CI/CD

```bash
# Connect GitHub repository (first time)
gcloud builds connect --region=us-central1 --name=train-backend

# Create build trigger
gcloud builds triggers create github \
  --name=train-backend-trigger \
  --repo-name=train-backend \
  --repo-owner=iagop03 \
  --branch-pattern="^main$" \
  --build-config=infrastructure/gcp/cloudbuild.yaml
```

### 9. Set Up DNS (Optional)

```bash
# Create Cloud DNS Zone
gcloud dns managed-zones create train-api \
  --dns-name=api.traingymtracker.com \
  --description="DNS zone for TrAIn API"

# Get nameservers
gcloud dns managed-zones describe train-api --format="value(nameServers[])"

# Update your domain registrar with these nameservers
```

### 10. Configure Load Balancer (Optional)

```bash
# Create health check
gcloud compute health-checks create https train-health-check \
  --request-path=/api/health \
  --port=443

# Create backend service
gcloud compute backend-services create train-backend-service \
  --global \
  --protocol=HTTPS \
  --health-checks=train-health-check \
  --session-affinity=CLIENT_IP
```

## Verification

### Check deployed services

```bash
# List Cloud Run services
gcloud run services list

# Check Cloud SQL instances
gcloud sql instances list

# View service account permissions
gcloud projects get-iam-policy $PROJECT_ID

# Check enabled APIs
gcloud services list --enabled
```

### Test the deployment

```bash
# Get Cloud Run service URL
SERVICE_URL=$(gcloud run services describe train-backend \
  --region=us-central1 \
  --format='value(status.url)')

# Test health endpoint
curl $SERVICE_URL/api/health

# Test with authentication
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" $SERVICE_URL/api/users
```

## Environment Configuration

Create `.env.gcp` file from template:

```bash
cp .env.gcp.example .env.gcp

# Edit with your values
nano .env.gcp
```

Required environment variables:
- `GCP_PROJECT_ID`
- `GCP_REGION`
- `CLOUD_SQL_INSTANCE`
- `DATABASE_NAME`
- `CLOUD_RUN_SERVICE_NAME`

## Monitoring and Logging

### View logs

```bash
# Cloud Run logs
gcloud run logs read train-backend --region=us-central1 --limit=50

# Cloud SQL logs
gcloud sql operations list --instance=train-postgres-primary

# Cloud Build logs
gcloud builds log $(gcloud builds list --limit=1 --format='value(id)')
```

### Set up monitoring alerts

```bash
# Create notification channel
gcloud alpha monitoring channels create \
  --display-name="TrAIn Alert Channel" \
  --type=email \
  --channel-labels=email_address=your-email@example.com

# Create alert policy
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="Cloud Run Error Rate" \
  --condition-display-name="High Error Rate"
```

## Cost Management

### Enable budget alerts

```bash
# Create budget
gcloud billing budgets create \
  --billing-account=$BILLING_ACCOUNT_ID \
  --display-name="TrAIn Monthly Budget" \
  --budget-amount=100 \
  --threshold-rule=percent=50 \
  --threshold-rule=percent=90 \
  --threshold-rule=percent=100
```

### View costs

```bash
# Enable cost analysis
gcloud compute project-info describe $PROJECT_ID \
  --format="value(labels)"
```

## Troubleshooting

### Cloud SQL Connection Issues

```bash
# Test connection
gcloud sql connect train-postgres-primary --user=app_user

# Check network connectivity
gcloud compute networks describe train-vpc

# Verify Cloud SQL Proxy access
gcloud sql instances describe train-postgres-primary --format="value(ipAddresses[].ipAddress)"
```

### Cloud Run Deployment Issues

```bash
# Check service status
gcloud run services describe train-backend --region=us-central1

# View recent revisions
gcloud run revisions list --service=train-backend --region=us-central1

# Check IAM permissions
gcloud projects get-iam-policy $PROJECT_ID \
  --flatten="bindings[].members" \
  --format="table(bindings.role)" \
  --filter="bindings.members:train-cloud-run@*"
```

### Build Failures

```bash
# View detailed build logs
gcloud builds log BUILD_ID --stream

# Retry build
gcloud builds submit --config=cloudbuild.yaml
```

## Cleanup (Caution!)

```bash
# Delete Cloud Run service
gcloud run services delete train-backend --region=us-central1

# Delete Cloud SQL instance
gcloud sql instances delete train-postgres-primary

# Delete VPC and subnets
gcloud compute networks delete train-vpc

# Delete entire project (if needed)
gcloud projects delete $PROJECT_ID
```

## Support and Resources

- [Google Cloud Documentation](https://cloud.google.com/docs)
- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Cloud SQL Documentation](https://cloud.google.com/sql/docs)
- [Terraform Google Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)

## Next Steps

1. Configure Keycloak for authentication
2. Set up MongoDB Atlas for analytics
3. Configure GitHub Actions for automated deployments
4. Enable custom domain and SSL certificate
5. Set up monitoring dashboards
6. Configure auto-scaling policies
7. Set up backup and disaster recovery procedures
