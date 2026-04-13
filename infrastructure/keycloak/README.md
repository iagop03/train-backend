# Keycloak Deployment on GCP (TRAIN-16)

This directory contains infrastructure-as-code for deploying Keycloak on a GCP VM e2-small instance with HTTPS/SSL configuration.

## Overview

- **Machine Type**: GCP e2-small (2 vCPU, 2GB RAM)
- **Region**: Configurable (default: us-central1)
- **Database**: Cloud SQL PostgreSQL 15
- **SSL/TLS**: Let's Encrypt with automatic renewal
- **Reverse Proxy**: Nginx
- **Java**: OpenJDK 21
- **Keycloak Version**: 23.0.0

## Prerequisites

### GCP Setup
1. GCP Project with billing enabled
2. gcloud CLI installed and configured
3. Terraform >= 1.0
4. DNS domain registered and configured in Google Cloud DNS
5. Cloud SQL Admin API enabled
6. Compute Engine API enabled

### Local Requirements
- Terraform
- gcloud CLI
- ssh client

## Deployment Steps

### 1. Prepare Configuration

```bash
cd infrastructure/keycloak
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your values:

```hcl
gcp_project_id           = "your-project-id"
keycloak_admin_password  = "your-secure-password"
keycloak_domain          = "keycloak.yourdomain.com"
db_username              = "keycloak_user"
db_password              = "your-secure-db-password"
letsencrypt_email        = "admin@yourdomain.com"
dns_managed_zone_name    = "your-dns-zone"
```

### 2. Initialize Terraform

```bash
terraform init
terraform plan -out=tfplan
```

Review the plan and ensure everything is correct.

### 3. Deploy Infrastructure

```bash
terraform apply tfplan
```

This will:
- Create a GCP e2-small VM instance
- Create Cloud SQL PostgreSQL instance
- Configure networking and firewall rules
- Reserve a static IP address
- Create DNS records
- Execute startup script for Keycloak installation

### 4. Monitor Deployment Progress

```bash
# Get VM IP
gcloud compute instances describe keycloak-server --zone=us-central1-a --format='value(networkInterfaces[0].accessConfigs[0].natIp)'

# SSH into VM
gcloud compute ssh keycloak-server --zone=us-central1-a

# Check startup script logs
sudo tail -f /var/log/keycloak-startup.log

# Check Keycloak service status
sudo systemctl status keycloak
sudo journalctl -u keycloak -f
```

### 5. Verify Deployment

```bash
# Check if Keycloak is responding
curl -k https://keycloak.yourdomain.com/health

# Access Admin Console
# Open in browser: https://keycloak.yourdomain.com/admin
# Login with credentials from terraform.tfvars
```

## Configuration Files

### keycloak.conf
Main Keycloak configuration file (auto-generated in `/opt/keycloak/conf/`)

**Key Settings:**
- `db`: PostgreSQL
- `hostname`: Your domain
- `proxy`: reencrypt (for reverse proxy)
- `https-port`: 8443
- `metrics-enabled`: true
- `health-enabled`: true

### systemd Service
Keycloak runs as a systemd service for automatic startup and restart.

**Service file**: `/etc/systemd/system/keycloak.service`

**Common commands:**
```bash
sudo systemctl start keycloak
sudo systemctl stop keycloak
sudo systemctl restart keycloak
sudo systemctl status keycloak
```

### Nginx Configuration
Reverse proxy with SSL termination.

**Config file**: `/etc/nginx/sites-available/keycloak`

**Features:**
- HTTP to HTTPS redirect
- TLS 1.2/1.3
- WebSocket support
- Proxy headers configuration

## SSL/TLS Certificate Management

### Let's Encrypt
SSL certificates are automatically managed using Let's Encrypt and certbot.

**Certificate path**: `/etc/letsencrypt/live/keycloak.yourdomain.com/`

**Auto-renewal**: Configured via systemd timer (daily)

**Manual renewal:**
```bash
sudo certbot renew
```

**Renewal hook:** `/etc/letsencrypt/renewal-hooks/post/keycloak-restart.sh`
Automatically restarts Nginx after certificate renewal.

## Database Connection

### Cloud SQL PostgreSQL
- **Instance**: keycloak-db
- **Version**: PostgreSQL 15
- **Tier**: db-f1-micro
- **Availability**: Regional (High Availability)
- **Backups**: Daily with 7-day retention

### Connection Details
```
Host: [Cloud SQL Private IP]
Port: 5432
Database: keycloak
User: keycloak_user
Password: [From terraform.tfvars]
```

## Logs and Monitoring

### Keycloak Logs
```bash
# Real-time logs
sudo journalctl -u keycloak -f

# Log file
sudo tail -f /var/log/keycloak/keycloak.log

# Startup logs
sudo cat /var/log/keycloak-startup.log
```

### Nginx Logs
```bash
# Access logs
sudo tail -f /var/log/nginx/access.log

# Error logs
sudo tail -f /var/log/nginx/error.log
```

### GCP Cloud Logging
```bash
# View VM logs in Cloud Logging
gcloud logging read "resource.type=gce_instance AND resource.labels.instance_id=keycloak-server"

# Stream logs
gcloud logging read "resource.type=gce_instance" --limit 50 --follow
```

## Initial Setup

### 1. Create Realm for TrAIn

```bash
# Access admin console
https://keycloak.yourdomain.com/admin

# Create new realm
- Realm name: train-gym
- Enabled: Yes
```

### 2. Configure Client for Backend

In Keycloak Admin Console:
1. Select realm: **train-gym**
2. Clients > Create client
3. Client ID: `train-backend`
4. Client Protocol: `openid-connect`
5. Configure:
   - Client authentication: ON
   - Authorization: ON
   - Valid redirect URIs: `https://api.yourdomain.com/*`
   - Web Origins: `https://api.yourdomain.com`

### 3. Configure Client for Web (Angular)

1. Client ID: `train-web`
2. Client Protocol: `openid-connect`
3. Client authentication: OFF (Public client)
4. Valid redirect URIs:
   - `https://app.yourdomain.com/callback`
   - `http://localhost:4200/callback` (development)
5. Web Origins:
   - `https://app.yourdomain.com`
   - `http://localhost:4200` (development)

### 4. Configure Client for Mobile (Flutter)

1. Client ID: `train-mobile`
2. Client Protocol: `openid-connect`
3. Client authentication: OFF (Public client)
4. Valid redirect URIs:
   - `com.train.gym://oauth-callback`
   - `io.train.gym://oauth-callback`
5. Advanced Settings > pkce_required: ON

## Environment Variables for Applications

### Backend (Spring Boot)
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://keycloak.yourdomain.com/realms/train-gym
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://keycloak.yourdomain.com/realms/train-gym/protocol/openid-connect/certs

keycloak.server-url=https://keycloak.yourdomain.com
keycloak.realm=train-gym
keycloak.client-id=train-backend
keycloak.client-secret=<from-keycloak-admin-console>
```

### Web (Angular)
```typescript
export const environment = {
  keycloak: {
    url: 'https://keycloak.yourdomain.com',
    realm: 'train-gym',
    clientId: 'train-web'
  }
};
```

### Mobile (Flutter)
```dart
final keycloakUrl = 'https://keycloak.yourdomain.com';
final realm = 'train-gym';
final clientId = 'train-mobile';
```

## Maintenance

### Health Checks
```bash
# Keycloak health endpoint
curl https://keycloak.yourdomain.com/health

# Nginx status
sudo systemctl status nginx

# SSL certificate expiration
sudo certbot certificates
```

### Disk Space Monitoring
```bash
# Check disk usage
df -h

# Check Keycloak logs size
du -sh /var/log/keycloak/

# Rotate old logs if needed
sudo logrotate -f /etc/logrotate.d/keycloak
```

### Database Backups
```bash
# Cloud SQL automatic backups are enabled
# View backups in GCP Console:
# Cloud SQL > keycloak-db > Backups

# Manual backup
gcloud sql backups create --instance=keycloak-db
```

### Updates

#### Update Keycloak
```bash
# Check current version
/opt/keycloak/bin/kc.sh --version

# For major/minor updates:
# 1. Stop Keycloak: sudo systemctl stop keycloak
# 2. Backup database
# 3. Download new version
# 4. Update configuration if needed
# 5. Start Keycloak: sudo systemctl start keycloak
```

#### Update OS Packages
```bash
sudo apt-get update
sudo apt-get upgrade -y
sudo systemctl restart keycloak
```

## Troubleshooting

### Keycloak Won't Start

```bash
# Check Java installation
java -version

# Check service logs
sudo journalctl -u keycloak -n 50

# Check disk space
df -h

# Increase Java memory if needed
# Edit /etc/systemd/system/keycloak.service
# Change JAVA_OPTS if out of memory
```

### SSL Certificate Issues

```bash
# Check certificate status
sudo certbot certificates

# Renew certificate manually
sudo certbot renew --force-renewal

# Check Nginx configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

### Database Connection Issues

```bash
# Test connection
psql -h [db-host] -U keycloak_user -d keycloak -c "SELECT 1"

# Check network connectivity
gcloud sql instances describe keycloak-db --format='value(ipAddresses[0].ipAddress)'

# Check firewall rules
gcloud compute firewall-rules list --filter="network:default"
```

### High CPU/Memory Usage

```bash
# Monitor real-time usage
top

# Check Keycloak process
ps aux | grep keycloak

# Adjust Java heap size in /etc/systemd/system/keycloak.service
# Current: -Xms256m -Xmx512m
# For e2-small (2GB): max to -Xmx1024m
```

## Cleanup

To destroy all resources:

```bash
terraform destroy
```

Note: This will delete:
- VM instance
- Static IP (new one will be allocated if recreated)
- Cloud SQL instance (protected by deletion_protection flag)
- Firewall rules
- DNS records

## Cost Estimation

### GCP e2-small VM
- Compute: ~$10-15/month
- Storage (50GB): ~$2-3/month

### Cloud SQL db-f1-micro
- Database: ~$10-15/month
- Storage: ~$1-2/month
- Backup: Included

### Networking
- Static IP: ~$1/month (when not attached)
- Cloud NAT (if used): ~$32 + data

**Total**: ~$25-35/month (regional setup)

## Support and Documentation

- [Keycloak Official Documentation](https://www.keycloak.org/documentation)
- [GCP Cloud SQL Documentation](https://cloud.google.com/sql/docs)
- [Terraform Google Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)

## Security Best Practices

1. **Change default passwords** immediately after deployment
2. **Restrict SSH access** in firewall rules to specific IPs
3. **Enable MFA** in Keycloak admin account
4. **Use strong passwords** for database and admin users
5. **Enable audit logging** in Keycloak
6. **Keep backups** of SSL certificates and configuration
7. **Monitor logs** regularly for suspicious activity
8. **Use VPC Service Controls** if available (GCP)
9. **Implement rate limiting** for auth endpoints
10. **Keep software updated** with security patches
