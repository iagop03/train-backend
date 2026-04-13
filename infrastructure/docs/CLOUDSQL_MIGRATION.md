# Cloud SQL Migration Guide

Guía para migrar datos de PostgreSQL existente a Cloud SQL.

## Opciones de Migración

### 1. Database Migration Service (Recomendado)

Mejor para grandes bases de datos con downtime mínimo.

```bash
# Crear conexión de origen (on-prem PostgreSQL)
gcloud database-migration connection-profiles create postgres-source \
  --location=us-central1 \
  --type=POSTGRES \
  --display-name="Source PostgreSQL" \
  --host=source.example.com \
  --port=5432 \
  --username=postgres

# Crear conexión destino (Cloud SQL)
gcloud database-migration connection-profiles create postgres-dest \
  --location=us-central1 \
  --type=CLOUDSQL \
  --cloudsql-instance=$CLOUDSQL_INSTANCE

# Crear migration job
gcloud database-migration migration-jobs create train-migration \
  --location=us-central1 \
  --source=postgres-source \
  --destination=postgres-dest \
  --type=CONTINUOUS
```

### 2. pgdump + psql (Para bases de datos pequeñas)

```bash
# Exportar desde source
pg_dump -h source.example.com -U postgres -d train \
  --no-owner --no-privileges \
  > train-backup.sql

# Importar a Cloud SQL
psql -h cloudsql-private-ip -U train_app -d train \
  < train-backup.sql
```

### 3. Logical Replication

```bash
# Setup en source
ALTER SYSTEM SET wal_level = logical;
ALTER SYSTEM SET max_replication_slots = 10;

SELECT pg_reload_conf();

# Crear publicación
CREATE PUBLICATION train_pub FOR ALL TABLES;

# En Cloud SQL crear subscripción
CREATE SUBSCRIPTION train_sub CONNECTION 
  'host=source.example.com port=5432 user=postgres password=pass dbname=train'
  PUBLICATION train_pub;
```

## Pre-Migration Checklist

- [ ] Backup de base de datos source
- [ ] Test de conectividad VPN/VPC
- [ ] Validación de caracteres y encoding
- [ ] Verificar versión PostgreSQL compatibility
- [ ] Revisar custom extensions requeridas
- [ ] Plan de rollback
- [ ] Ventana de mantenimiento comunicada

## Post-Migration

```bash
# Validar datos
psql -c "SELECT count(*) FROM train.*;"

# Verificar integridad
psql -c "ANALYZE;"

# Revisar índices
psql -c "REINDEX DATABASE train;"

# Actualizar connection strings
# En aplicación:
spring.datasource.url=jdbc:postgresql://cloudsql-private-ip:5432/train
```
