# TrAIn Backend - Environment Configuration

## Overview

The TrAIn backend is configured to support three environments:

- **DEV**: Local development with minimal security, full logging and hot reload
- **STAGING**: Pre-production environment with production-like configuration for testing
- **PRODUCTION**: Live environment with full security, performance optimization and monitoring

## Environment Profiles

### Activation

Environments are activated via the `SPRING_PROFILES_ACTIVE` environment variable:

```bash
# Development
export SPRING_PROFILES_ACTIVE=dev

# Staging
export SPRING_PROFILES_ACTIVE=staging

# Production
export SPRING_PROFILES_ACTIVE=production
```

### Configuration Files

Each environment has its own configuration file:

- `application.yml` - Base configuration shared by all environments
- `application-dev.yml` - Development-specific settings
- `application-staging.yml` - Staging-specific settings
- `application-production.yml` - Production-specific settings

## GCP Secret Manager Integration

Staging and production use GCP Secret Manager for sensitive configuration:

### Required Secrets

**Staging Environment:**
- `train-db-password-staging`
- `train-mongodb-uri-staging`
- `train-keycloak-url-staging`
- `train-keycloak-secret-staging`

**Production Environment:**
- `train-db-password-production`
- `train-mongodb-uri-production`
- `train-keycloak-url-production`
- `train-keycloak-secret-production`

### Create Secrets (GCP CLI)

```bash
# Set project
export PROJECT_ID=train-ai-gym
gcloud config set project $PROJECT_ID

# Create secrets (staging)
echo -n "your-db-password" | gcloud secrets create train-db-password-staging --data-file=-
echo -n "mongodb+srv://user:pass@cluster.mongodb.net/train_staging" | gcloud secrets create train-mongodb-uri-staging --data-file=-
echo -n "https://keycloak-staging.train-ai-gym.com" | gcloud secrets create train-keycloak-url-staging --data-file=-
echo -n "your-keycloak-secret" | gcloud secrets create train-keycloak-secret-staging --data-file=-

# Create secrets (production)
echo -n "your-db-password" | gcloud secrets create train-db-password-production --data-file=-
echo -n "mongodb+srv://user:pass@cluster.mongodb.net/train" | gcloud secrets create train-mongodb-uri-production --data-file=-
echo -n "https://keycloak.train-ai-gym.com" | gcloud secrets create train-keycloak-url-production --data-file=-
echo -n "your-keycloak-secret" | gcloud secrets create train-keycloak-secret-production --data-file=-
```

### Grant Access

```bash
# Service account that will access secrets
SERVICE_ACCOUNT=train-backend-prod@${PROJECT_ID}.iam.gserviceaccount.com

# Grant Secret Accessor role
gcloud secrets add-iam-policy-binding train-db-password-production \
  --member=serviceAccount:${SERVICE_ACCOUNT} \
  --role=roles/secretmanager.secretAccessor
```

## Local Development Setup

### Prerequisites

- Java 21+
- PostgreSQL 15+
- MongoDB 6+
- Docker & Docker Compose

### Quick Start

1. Clone repository:
```bash
git clone https://github.com/iagop03/train-backend.git
cd train-backend
```

2. Create `.env` file:
```bash
cp .env.example .env
```

3. Start services with Docker Compose:
```bash
docker-compose up -d
```

4. Build and run:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

5. Access the application:
```
http://localhost:8080/api/health
```

### Docker Compose (Development)

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: train_user
      POSTGRES_PASSWORD: train_pass_dev
      POSTGRES_DB: train_dev
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:7
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: mongoadmin
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

volumes:
  postgres_data:
  mongodb_data:
```

## Staging Environment

### Deployment

```bash
# Build and push to GCP Container Registry
gcloud builds submit --config=deploy/cloudbuild-staging.yaml
```

### Monitoring

- Cloud Logging: https://console.cloud.google.com/logs
- Cloud Monitoring: https://console.cloud.google.com/monitoring

### Database Access

```bash
# Connect to staging database via Cloud SQL Proxy
cloud_sql_proxy -instances=train-ai-gym:us-central1:train-db-staging=tcp:5432
```

## Production Environment

### Deployment

```bash
# Build and push to GCP Container Registry with production profile
gcloud builds submit --config=deploy/cloudbuild-production.yaml --substitutions="_ENVIRONMENT=production"
```

### Pre-Deployment Checklist

- [ ] All tests passing
- [ ] Code review approved
- [ ] Database migrations tested
- [ ] Staging verification complete
- [ ] Monitoring alerts configured
- [ ] Backup verified
- [ ] Rollback plan documented

### Monitoring & Alerting

```bash
# Set up monitoring
gcloud monitoring policies create --display-name="Train Backend Health" \
  --condition-display-name="HTTP 5xx Errors" \
  --notification-channels=CHANNEL_ID
```

### Health Checks

```bash
# Verify deployment
curl https://api.train-ai-gym.com/api/health
```

## Environment-Specific Features

### Development
- ✅ DDL auto-generation (create-drop)
- ✅ SQL logging
- ✅ Debug level logging
- ✅ CORS disabled (localhost only)
- ✅ JWT validation disabled
- ✅ Error stack traces included

### Staging
- ✅ DDL validation only
- ✅ Connection pooling (10 connections)
- ✅ Info level logging
- ✅ CORS restricted to staging domains
- ✅ JWT validation enabled
- ✅ Error messages sanitized
- ✅ GCP Secret Manager integration

### Production
- ✅ DDL validation only
- ✅ Connection pooling (20 connections)
- ✅ Warn level logging
- ✅ CORS restricted to production domains
- ✅ JWT validation enabled
- ✅ HTTPS enforced
- ✅ No stack traces in responses
- ✅ GCP Secret Manager integration
- ✅ Advanced performance optimization
- ✅ Comprehensive monitoring

## Troubleshooting

### Secret Manager Access Denied

```bash
# Check service account permissions
gcloud iam service-accounts get-iam-policy \
  train-backend-prod@${PROJECT_ID}.iam.gserviceaccount.com

# Grant missing permissions
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:train-backend-prod@${PROJECT_ID}.iam.gserviceaccount.com \
  --role=roles/secretmanager.secretAccessor
```

### Database Connection Issues

```bash
# Test connection
psql -h localhost -U train_user -d train_dev -c "SELECT version();"
```

### Keycloak Configuration

```bash
# Verify Keycloak realm configuration
curl https://keycloak.train-ai-gym.com/auth/realms/train/.well-known/openid-configuration
```

## References

- [Spring Boot Profiles](https://spring.io/blog/2015/02/16/easier-multitenant-applications-with-spring-boot-1-2-0)
- [GCP Secret Manager](https://cloud.google.com/secret-manager/docs)
- [Spring Cloud GCP](https://github.com/GoogleCloudPlatform/spring-cloud-gcp)
