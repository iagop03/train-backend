-- TrAIn Database Initialization Script
-- PostgreSQL 15+

-- Set timezone
SET timezone TO 'UTC';

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
CREATE EXTENSION IF NOT EXISTS "btree_gist";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS train_schema AUTHORIZATION postgres;
GRANT USAGE ON SCHEMA train_schema TO train_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA train_schema GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO train_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA train_schema GRANT USAGE, SELECT ON SEQUENCES TO train_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA train_schema GRANT EXECUTE ON FUNCTIONS TO train_app;

-- Create users table
CREATE TABLE IF NOT EXISTS train_schema.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    password_hash VARCHAR(255),
    phone VARCHAR(20),
    avatar_url TEXT,
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON train_schema.users(email);
CREATE INDEX idx_users_username ON train_schema.users(username);
CREATE INDEX idx_users_is_active ON train_schema.users(is_active);
CREATE INDEX idx_users_created_at ON train_schema.users(created_at);

-- Create workouts table
CREATE TABLE IF NOT EXISTS train_schema.workouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES train_schema.users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    workout_type VARCHAR(50) NOT NULL,
    duration_minutes INTEGER,
    calories_burned DECIMAL(10, 2),
    intensity_level VARCHAR(20),
    notes TEXT,
    completed BOOLEAN DEFAULT false,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_workouts_user_id ON train_schema.workouts(user_id);
CREATE INDEX idx_workouts_scheduled_at ON train_schema.workouts(scheduled_at);
CREATE INDEX idx_workouts_completed ON train_schema.workouts(completed);
CREATE INDEX idx_workouts_created_at ON train_schema.workouts(created_at);
CREATE INDEX idx_workouts_user_created ON train_schema.workouts(user_id, created_at);

-- Create exercises table
CREATE TABLE IF NOT EXISTS train_schema.exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_id UUID NOT NULL REFERENCES train_schema.workouts(id) ON DELETE CASCADE,
    exercise_name VARCHAR(255) NOT NULL,
    sets INTEGER NOT NULL,
    reps INTEGER,
    weight_kg DECIMAL(10, 2),
    duration_seconds INTEGER,
    distance_km DECIMAL(10, 3),
    heart_rate_avg INTEGER,
    heart_rate_max INTEGER,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exercises_workout_id ON train_schema.exercises(workout_id);
CREATE INDEX idx_exercises_exercise_name ON train_schema.exercises(exercise_name);

-- Create audit log table
CREATE TABLE IF NOT EXISTS train_schema.audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES train_schema.users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON train_schema.audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON train_schema.audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON train_schema.audit_logs(created_at);

-- Grant permissions
GRANT USAGE ON SCHEMA train_schema TO train_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA train_schema TO train_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA train_schema TO train_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA train_schema TO train_app;

-- Set search path
ALTER USER train_app SET search_path = train_schema, public;
