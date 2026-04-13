# Database Migrations - TrAIn Backend

## Overview

This project uses **Flyway** for database schema versioning and migrations. Flyway automatically manages all database schema changes in a version-controlled manner.

## Architecture

### Migration Storage
- **Location**: `src/main/resources/db/migration/`
- **Naming Convention**: `V{version}__{description}.sql`
- **Versioning**: Semantic versioning (V1, V2, V3, etc.)

### Migration Tracking
Flyway maintains a `flyway_schema_history` table that tracks:
- Migration version
- Description
- Type (SQL or JDBC)
- Execution time
- Success/failure status

## Current Migrations

### V1__Initial_Schema.sql
Initial database schema for TrAIn application:
- **User Management**: User profiles, roles, permissions
- **Workout Tracking**: Workouts, exercises, sets
- **Goal Management**: Fitness goals and progress tracking
- **Nutrition**: Nutrition logs and meal tracking
- **Social Features**: Followers, achievements
- **System Tables**: Audit logs, notifications, tokens

**Key Features**:
- UUID primary keys for all tables
- Automatic timestamp management (created_at, updated_at)
- Foreign key constraints with CASCADE delete
- Comprehensive indexing for performance
- Soft deletes via deleted_at column
- JSONB support for flexible data

### V2__Insert_Default_Roles.sql
Inserts default system roles:
- ADMIN
- TRAINER
- USER
- MODERATOR
- NUTRITIONIST

### V3__Insert_Sample_Achievements.sql
Inserts sample achievement badges:
- Workout completion achievements
- Streak achievements
- Social achievements
- Calorie burning achievements

## Configuration

### application.yml
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false
    validate-on-migrate: true
    clean-disabled: true
    locations: classpath:db/migration
```

### Key Settings
- **enabled**: Enable/disable Flyway
- **baseline-on-migrate**: Create baseline on first run (disabled)
- **validate-on-migrate**: Validate migrations before running
- **clean-disabled**: Prevent accidental database cleaning (always disabled in production)
- **locations**: Migration file locations

## Running Migrations

### Automatic (on application startup)
```bash
mvn spring-boot:run
```
Flyway runs automatically when Spring Boot starts.

### Manual with Maven
```bash
# Validate migrations
mvn flyway:validate

# Show migration info
mvn flyway:info

# Run migrations
mvn flyway:migrate

# Repair (fix metadata issues - use with caution)
mvn flyway:repair
```

### Manual with Gradle
```gradle
build.gradle:
tools {
    flyway {
        url = 'jdbc:postgresql://localhost:5432/train_db'
        user = 'train_user'
        password = 'train_password'
    }
}
```

## Creating New Migrations

### 1. Create SQL File
Create file: `src/main/resources/db/migration/V4__Add_new_feature.sql`

```sql
-- V4__Add_new_feature.sql
-- Description: Add new feature columns
-- Created: 2024-01-20

ALTER TABLE workout ADD COLUMN IF NOT EXISTS new_column VARCHAR(255);
CREATE INDEX idx_workout_new_column ON workout(new_column);
```

### 2. Naming Convention
- **Prefix**: `V` (uppercase)
- **Version**: Numeric (V1, V2, V3, etc.)
- **Separator**: `__` (double underscore)
- **Description**: Descriptive text in snake_case
- **Suffix**: `.sql`

### 3. Best Practices

#### Use IF NOT EXISTS
```sql
ALTER TABLE table_name ADD COLUMN IF NOT EXISTS column_name TYPE;
CREATE INDEX IF NOT EXISTS idx_name ON table(column);
CREATE TABLE IF NOT EXISTS table_name (...);
```

#### Be Idempotent
Migrations should be safe to run multiple times without error.

#### Add Comments
```sql
-- V5__Feature_description.sql
-- Purpose: Explain what this migration does
-- Dependencies: List any related migrations
-- Rollback: Explain how to rollback if needed
```

#### Handle Data Migrations
```sql
-- Add new column
ALTER TABLE users ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';

-- Update existing data
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- Add constraint
ALTER TABLE users ALTER COLUMN status SET NOT NULL;
```

## Database Connection

### Development
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/train_db_dev
    username: train_user
    password: train_password
```

### Production (GCP Cloud SQL)
```bash
export DB_HOST=cloudsql-instance-ip
export DB_USER=train_user
export DB_PASSWORD=<secure-password>
export DB_NAME=train_db
```

## Troubleshooting

### Failed Migrations
If a migration fails:

1. **Check Status**
   ```bash
   mvn flyway:info
   ```

2. **Repair Metadata** (if migration was partially applied)
   ```bash
   mvn flyway:repair
   ```

3. **Manual Fix**
   - Connect to database
   - Manually apply the migration
   - Mark as successful in `flyway_schema_history`

### Validation Errors
```bash
# Validate pending migrations
mvn flyway:validate
```

Common issues:
- Unsupported SQL syntax
- Missing files
- Checksum mismatches

### Checksum Mismatches
If a migration file was modified:
```bash
mvn flyway:repair
```

## CI/CD Integration

### GitHub Actions
See `.github/workflows/database-migration.yml` for automated testing.

**Workflow**:
1. Validate migration syntax
2. Create test database
3. Run migrations
4. Verify schema
5. Run integration tests

### Testing Migrations
```bash
# Run migrations on test database
mvn clean test -Dspring.datasource.url=jdbc:postgresql://localhost:5432/train_db_test
```

## Performance Considerations

### Indexing Strategy
- Index all foreign key columns
- Index frequently queried columns
- Index sort/filter columns
- Use composite indexes for common queries

### Large Data Migrations
```sql
-- Add column without constraint
ALTER TABLE large_table ADD COLUMN new_column TYPE;

-- Update in batches
WITH batch AS (
  SELECT id FROM large_table 
  WHERE new_column IS NULL 
  LIMIT 10000
)
UPDATE large_table SET new_column = value 
WHERE id IN (SELECT id FROM batch);

-- Add constraint after data migration
ALTER TABLE large_table ALTER COLUMN new_column SET NOT NULL;
```

## Backup Strategy

### Before Running Migrations
```bash
# Backup database
pg_dump -h localhost -U train_user -d train_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restore from Backup
```bash
psql -h localhost -U train_user -d train_db < backup_file.sql
```

## Monitoring

### View Migration History
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
```

### Check Migration Status
```bash
mvn flyway:info
```

## Documentation Links

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Boot Flyway Integration](https://spring.io/blog/2021/06/21/database-versioning-with-spring-boot-and-flyway)
