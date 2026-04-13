# Cloud SQL PostgreSQL Setup Guide

Documentation completa para provisionar y gestionar Cloud SQL PostgreSQL con VPC privada.

## 📋 Tabla de Contenidos

1. [Configuración Inicial](#configuración-inicial)
2. [Arquitectura](#arquitectura)
3. [Provisioning con Terraform](#provisioning-con-terraform)
4. [Configuración Manual](#configuración-manual)
5. [Monitoreo y Alertas](#monitoreo-y-alertas)
6. [Backups y Recuperación](#backups-y-recuperación)
7. [Seguridad](#seguridad)
8. [Troubleshooting](#troubleshooting)

## Configuración Inicial

### Requisitos Previos

- GCP Project con Cloud SQL Admin API habilitada
- Terraform >= 1.0
- gcloud CLI configurado
- kubectl (para Kubernetes)

### Variables de Entorno

```bash
export GCP_PROJECT_ID="train-project-12345"
export GCP_REGION="us-central1"
export CLOUDSQL_INSTANCE="train-postgres-prod"
```

## Arquitectura

### Componentes

```
┌─────────────────────────────────────────┐
│         Google Cloud VPC                │
│  ┌──────────────────────────────────┐   │
│  │   App Subnet (10.0.0.0/24)      │   │
│  │  - GKE Pods                     │   │
│  │  - App Engine                   │   │
│  └──────────────┬───────────────────┘   │
│                 │                       │
│  ┌──────────────▼──────────────────┐   │
│  │  CloudSQL Subnet (10.1.0.0/24)  │   │
│  │  - PostgreSQL Instance          │   │
│  │  - Private IP: 10.1.0.x         │   │
│  │  - SSL/TLS Required             │   │
│  │  - High Availability (Regional) │   │
│  │  - Automated Backups (Daily)    │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘

┌────────────────────────────────────────────┐
│    Cloud SQL Proxy (K8s Deployment)        │
│  - Acts as connection pooler               │
│  - IAM authentication                      │
│  - Metrics export to Prometheus            │
└────────────────────────────────────────────┘
```

## Provisioning con Terraform

### 1. Inicializar Terraform

```bash
cd infrastructure/terraform
terraform init -backend-config="bucket=train-project-terraform-state"
```

### 2. Crear archivo terraform.tfvars

```bash
cp terraform.tfvars.example terraform.tfvars
# Editar con valores reales
vim terraform.tfvars
```

### 3. Plan y Apply

```bash
# Revisar cambios
terraform plan -out=tfplan

# Aplicar configuración
terraform apply tfplan
```

### 4. Exportar Outputs

```bash
# Obtener credenciales
terraform output -raw root_password > /tmp/root_password
terraform output -raw app_password > /tmp/app_password
terraform output -raw cloudsql_instance_connection_name
terraform output -raw cloudsql_private_ip
```

## Configuración Manual

### Setup Script

```bash
# Ejecutar script de setup
cd infrastructure/scripts
chmod +x cloudsql-setup.sh
GCP_PROJECT_ID=$GCP_PROJECT_ID \
GCP_REGION=$GCP_REGION \
./cloudsql-setup.sh
```

### Configuración Manual de BD

#### 1. Conectar a la instancia

Usando Cloud SQL Proxy:

```bash
# Instalar Cloud SQL Proxy
curl -o cloud-sql-proxy https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64
chmod +x cloud-sql-proxy

# Ejecutar proxy
./cloud-sql-proxy --instances=$PROJECT_ID:$REGION:$INSTANCE_NAME

# En otra terminal, conectar con psql
psql -h 127.0.0.1 -U postgres -d train
```

#### 2. Crear roles y usuarios

```sql
-- Conectar como postgres
psql -h 127.0.0.1 -U postgres

-- Crear rol para aplicación
CREATE ROLE train_app WITH LOGIN PASSWORD 'secure_password';

-- Crear base de datos
CREATE DATABASE train OWNER train_app;

-- Conectar a la BD
\c train

-- Crear schema
CREATE SCHEMA IF NOT EXISTS train AUTHORIZATION train_app;

-- Otorgar permisos
GRANT USAGE ON SCHEMA train TO train_app;
GRANT CREATE ON SCHEMA train TO train_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA train TO train_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA train TO train_app;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA train TO train_app;

-- Permisos por defecto para nuevos objetos
ALTER DEFAULT PRIVILEGES IN SCHEMA train GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO train_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA train GRANT USAGE, SELECT ON SEQUENCES TO train_app;

-- Ver roles creados
\du
```

## Monitoreo y Alertas

### Métricas Disponibles

```bash
# Ver métricas en GCP Console
gcloud monitoring metrics-descriptors list --filter="metric.type:cloudsql*"
```

### Configurar Alertas

#### 1. CPU Alta (>80%)

```bash
gcloud alpha monitoring policies create \
  --notification-channels=$CHANNEL_ID \
  --display-name="Cloud SQL - High CPU" \
  --condition-display-name="CPU > 80%" \
  --condition-threshold-value=0.8 \
  --condition-threshold-duration=300s
```

#### 2. Storage Alto (>85%)

Ver `infrastructure/kubernetes/cloudsql-monitoring.yaml`

### Prometheus + Grafana

Aplicar manifiestos:

```bash
kubectl apply -f infrastructure/kubernetes/cloudsql-monitoring.yaml
```

## Backups y Recuperación

### Backups Automáticos

Configurados en Terraform:
- **Frecuencia**: Diariamente a las 03:00 UTC
- **Retención**: 30 backups
- **Point-in-Time Recovery**: Habilitado (7 días)
- **Transaction Logs**: Retención de 7 días

### Crear Backup Manual

```bash
chmod +x infrastructure/scripts/cloudsql-backup.sh
GCP_PROJECT_ID=$GCP_PROJECT_ID \
CLOUDSQL_INSTANCE=$CLOUDSQL_INSTANCE \
./infrastructure/scripts/cloudsql-backup.sh
```

### Listar Backups

```bash
gcloud sql backups list \
  --instance=$CLOUDSQL_INSTANCE \
  --project=$GCP_PROJECT_ID \
  --format='table(name,windowStartTime,status)'
```

### Restaurar desde Backup

```bash
chmod +x infrastructure/scripts/cloudsql-restore.sh
GCP_PROJECT_ID=$GCP_PROJECT_ID \
CLOUDSQL_INSTANCE=$CLOUDSQL_INSTANCE \
./infrastructure/scripts/cloudsql-restore.sh backup-20240115-030000
```

### Exportar a Cloud Storage

```bash
# Crear bucket
gsutil mb gs://train-backups

# Exportar base de datos
gcloud sql export sql $CLOUDSQL_INSTANCE \
  gs://train-backups/train-$(date +%Y%m%d-%H%M%S).sql \
  --database=train \
  --project=$GCP_PROJECT_ID
```

## Seguridad

### VPC Privada

✅ **Implementado en Terraform**
- Cloud SQL instancia sin IP pública
- Acceso solo a través de Private Service Connection
- Firewall rules restringen tráfico interno

### SSL/TLS

✅ **Implementado**
- SSL requerido para todas las conexiones
- Certificados cliente autofirmados
- Server CA cert proporcionado

```bash
# Obtener certificados
terraform output -raw ssl_server_ca_cert > server-ca.pem
terraform output -raw ssl_client_cert > client-cert.pem
terraform output -raw ssl_client_key > client-key.pem

# Usar en conexión
psql "sslmode=require sslcert=client-cert.pem sslkey=client-key.pem sslrootcert=server-ca.pem"
```

### IAM Authentication

✅ **Habilitado**

```sql
-- En PostgreSQL
CREATE USER "service-account@project.iam" WITH LOGIN;
GRANT ALL ON DATABASE train TO "service-account@project.iam";
```

```bash
# Desde GCP con IAM
gcloud sql connect $CLOUDSQL_INSTANCE \
  --user=cloudsql-iam-user \
  --use-cloud-sql-proxy
```

### Audit Logging

```sql
-- Habilitar en instancia
SET cloudsql.iam_authentication = on;
SET log_statement = 'all';
SET log_min_duration_statement = 1000; -- Log queries > 1s
```

## Troubleshooting

### Conexión Rechazada

```bash
# 1. Verificar IP privada de la instancia
gcloud sql instances describe $CLOUDSQL_INSTANCE \
  --format='value(ipAddresses[0].ipAddress)'

# 2. Verificar VPC peering
gcloud compute networks peerings list \
  --network=train-vpc

# 3. Verificar firewall rules
gcloud compute firewall-rules list \
  --filter="network:train-vpc"
```

### Errores de SSL

```bash
# Verificar certificado
openssl x509 -in client-cert.pem -text -noout

# Regenerar certificados
gcloud sql ssl-certs create train-cert-new \
  --instance=$CLOUDSQL_INSTANCE
```

### Performance Issues

```bash
# Ver índices
psql -c "SELECT * FROM pg_stat_user_indexes;"

# Ver query lenta
psql -c "EXPLAIN ANALYZE SELECT ..."

# Ver conexiones activas
psql -c "SELECT * FROM pg_stat_activity;"
```

### Espacio en Disco Bajo

```bash
# Ver tamaño de tablas
psql -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) 
FROM pg_tables ORDER BY pg_total_relation_size DESC LIMIT 10;"

# Realizar VACUUM
psql -c "VACUUM ANALYZE;"
```

## Referencias

- [Cloud SQL PostgreSQL Docs](https://cloud.google.com/sql/docs/postgres)
- [Private IP Connectivity](https://cloud.google.com/sql/docs/postgres/private-ip)
- [SSL Connections](https://cloud.google.com/sql/docs/postgres/ssl-certs)
- [Backups](https://cloud.google.com/sql/docs/postgres/backup-recovery)
