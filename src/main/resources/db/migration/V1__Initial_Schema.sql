-- V1__Initial_Schema.sql
-- Initial database schema for TrAIn (AI Gym Tracker)
-- Created: 2024-01-15

-- Create schema
CREATE SCHEMA IF NOT EXISTS public;

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ==================== USERS TABLE ====================
CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    avatar_url TEXT,
    bio TEXT,
    height_cm NUMERIC(5,2),
    weight_kg NUMERIC(6,2),
    age INT,
    gender VARCHAR(10),
    keycloak_id VARCHAR(255) UNIQUE,
    account_status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_keycloak_id ON "user"(keycloak_id);
CREATE INDEX idx_user_created_at ON "user"(created_at);
CREATE INDEX idx_user_deleted_at ON "user"(deleted_at) WHERE deleted_at IS NULL;

-- ==================== ROLES TABLE ====================
CREATE TABLE IF NOT EXISTS role (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_role_name ON role(name);

-- ==================== USER ROLES TABLE ====================
CREATE TABLE IF NOT EXISTS user_role (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    UNIQUE(user_id, role_id)
);

CREATE INDEX idx_user_role_user_id ON user_role(user_id);
CREATE INDEX idx_user_role_role_id ON user_role(role_id);

-- ==================== WORKOUTS TABLE ====================
CREATE TABLE IF NOT EXISTS workout (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    workout_type VARCHAR(50) NOT NULL CHECK (workout_type IN ('STRENGTH', 'CARDIO', 'FLEXIBILITY', 'SPORTS', 'CUSTOM')),
    duration_minutes INT,
    intensity_level VARCHAR(20) CHECK (intensity_level IN ('LOW', 'MODERATE', 'HIGH', 'VERY_HIGH')),
    calories_burned NUMERIC(8,2),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    location VARCHAR(255),
    notes TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_workout_user_id ON workout(user_id);
CREATE INDEX idx_workout_start_time ON workout(start_time);
CREATE INDEX idx_workout_created_at ON workout(created_at);
CREATE INDEX idx_workout_is_completed ON workout(is_completed);
CREATE INDEX idx_workout_deleted_at ON workout(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_workout_user_start_time ON workout(user_id, start_time DESC);

-- ==================== EXERCISES TABLE ====================
CREATE TABLE IF NOT EXISTS exercise (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_id UUID NOT NULL REFERENCES workout(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    muscle_group VARCHAR(100),
    sets INT,
    reps INT,
    weight_kg NUMERIC(8,2),
    distance_km NUMERIC(8,2),
    duration_seconds INT,
    rest_seconds INT,
    notes TEXT,
    sequence_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exercise_workout_id ON exercise(workout_id);
CREATE INDEX idx_exercise_muscle_group ON exercise(muscle_group);
CREATE INDEX idx_exercise_sequence ON exercise(workout_id, sequence_order);

-- ==================== EXERCISE SETS TABLE ====================
CREATE TABLE IF NOT EXISTS exercise_set (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exercise_id UUID NOT NULL REFERENCES exercise(id) ON DELETE CASCADE,
    set_number INT NOT NULL,
    actual_reps INT,
    actual_weight_kg NUMERIC(8,2),
    actual_duration_seconds INT,
    actual_distance_km NUMERIC(8,2),
    is_completed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exercise_set_exercise_id ON exercise_set(exercise_id);
CREATE INDEX idx_exercise_set_set_number ON exercise_set(exercise_id, set_number);

-- ==================== GOALS TABLE ====================
CREATE TABLE IF NOT EXISTS goal (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    goal_type VARCHAR(50) NOT NULL CHECK (goal_type IN ('WEIGHT_LOSS', 'MUSCLE_GAIN', 'ENDURANCE', 'STRENGTH', 'CUSTOM')),
    target_value NUMERIC(8,2),
    current_value NUMERIC(8,2),
    unit VARCHAR(50),
    start_date DATE NOT NULL,
    target_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABANDONED')),
    progress_percentage NUMERIC(5,2) DEFAULT 0,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_goal_user_id ON goal(user_id);
CREATE INDEX idx_goal_status ON goal(status);
CREATE INDEX idx_goal_created_at ON goal(created_at);

-- ==================== GOAL PROGRESS TABLE ====================
CREATE TABLE IF NOT EXISTS goal_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    goal_id UUID NOT NULL REFERENCES goal(id) ON DELETE CASCADE,
    progress_value NUMERIC(8,2) NOT NULL,
    progress_percentage NUMERIC(5,2),
    notes TEXT,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_goal_progress_goal_id ON goal_progress(goal_id);
CREATE INDEX idx_goal_progress_recorded_at ON goal_progress(recorded_at);

-- ==================== WORKOUT PLANS TABLE ====================
CREATE TABLE IF NOT EXISTS workout_plan (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty_level VARCHAR(20) CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
    duration_weeks INT,
    focus_area VARCHAR(100),
    is_public BOOLEAN DEFAULT FALSE,
    total_workouts INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_workout_plan_user_id ON workout_plan(user_id);
CREATE INDEX idx_workout_plan_created_at ON workout_plan(created_at);

-- ==================== WORKOUT PLAN WORKOUTS TABLE ====================
CREATE TABLE IF NOT EXISTS workout_plan_workout (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_plan_id UUID NOT NULL REFERENCES workout_plan(id) ON DELETE CASCADE,
    workout_name VARCHAR(255) NOT NULL,
    day_of_week VARCHAR(20),
    week_number INT,
    sequence_order INT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workout_plan_workout_plan_id ON workout_plan_workout(workout_plan_id);

-- ==================== ACHIEVEMENTS TABLE ====================
CREATE TABLE IF NOT EXISTS achievement (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    badge_level VARCHAR(20) CHECK (badge_level IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    criteria_type VARCHAR(100),
    criteria_value NUMERIC(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_achievement_badge_level ON achievement(badge_level);

-- ==================== USER ACHIEVEMENTS TABLE ====================
CREATE TABLE IF NOT EXISTS user_achievement (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievement(id) ON DELETE CASCADE,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, achievement_id)
);

CREATE INDEX idx_user_achievement_user_id ON user_achievement(user_id);
CREATE INDEX idx_user_achievement_achievement_id ON user_achievement(achievement_id);

-- ==================== NUTRITION LOGS TABLE ====================
CREATE TABLE IF NOT EXISTS nutrition_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    meal_type VARCHAR(50) CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK')),
    food_name VARCHAR(255) NOT NULL,
    calories NUMERIC(8,2),
    protein_g NUMERIC(8,2),
    carbs_g NUMERIC(8,2),
    fat_g NUMERIC(8,2),
    quantity_grams NUMERIC(8,2),
    logged_at TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_nutrition_log_user_id ON nutrition_log(user_id);
CREATE INDEX idx_nutrition_log_logged_at ON nutrition_log(logged_at);
CREATE INDEX idx_nutrition_log_user_logged_at ON nutrition_log(user_id, logged_at DESC);

-- ==================== STATISTICS TABLE ====================
CREATE TABLE IF NOT EXISTS user_statistics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    total_workouts INT DEFAULT 0,
    total_workout_duration_minutes INT DEFAULT 0,
    total_calories_burned NUMERIC(10,2) DEFAULT 0,
    average_workout_duration_minutes NUMERIC(8,2) DEFAULT 0,
    total_exercises_completed INT DEFAULT 0,
    personal_records INT DEFAULT 0,
    active_goals INT DEFAULT 0,
    completed_goals INT DEFAULT 0,
    current_streak_days INT DEFAULT 0,
    longest_streak_days INT DEFAULT 0,
    last_workout_date DATE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_statistics_user_id ON user_statistics(user_id);

-- ==================== AUDIT LOG TABLE ====================
CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES "user"(id) ON DELETE SET NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'VIEW')),
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);

-- ==================== FOLLOWERS TABLE ====================
CREATE TABLE IF NOT EXISTS follower (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    following_user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    followed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(follower_user_id, following_user_id),
    CHECK (follower_user_id != following_user_id)
);

CREATE INDEX idx_follower_follower_id ON follower(follower_user_id);
CREATE INDEX idx_follower_following_id ON follower(following_user_id);

-- ==================== REFRESH TOKENS TABLE ====================
CREATE TABLE IF NOT EXISTS refresh_token (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token(expires_at);
CREATE INDEX idx_refresh_token_revoked ON refresh_token(revoked);

-- ==================== NOTIFICATIONS TABLE ====================
CREATE TABLE IF NOT EXISTS notification (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),
    related_entity_type VARCHAR(100),
    related_entity_id UUID,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_notification_created_at ON notification(created_at);
CREATE INDEX idx_notification_user_read ON notification(user_id, is_read);

-- ==================== FUNCTIONS ====================
-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ==================== TRIGGERS ====================
-- User updated_at trigger
CREATE TRIGGER user_updated_at BEFORE UPDATE ON "user"
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Workout updated_at trigger
CREATE TRIGGER workout_updated_at BEFORE UPDATE ON workout
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Exercise updated_at trigger
CREATE TRIGGER exercise_updated_at BEFORE UPDATE ON exercise
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Goal updated_at trigger
CREATE TRIGGER goal_updated_at BEFORE UPDATE ON goal
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Workout plan updated_at trigger
CREATE TRIGGER workout_plan_updated_at BEFORE UPDATE ON workout_plan
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- User statistics updated_at trigger
CREATE TRIGGER user_statistics_updated_at BEFORE UPDATE ON user_statistics
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Role updated_at trigger
CREATE TRIGGER role_updated_at BEFORE UPDATE ON role
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==================== COMMENTS ====================
COMMENT ON TABLE "user" IS 'Main user table with profile information';
COMMENT ON TABLE workout IS 'User workout sessions';
COMMENT ON TABLE exercise IS 'Exercises within a workout';
COMMENT ON TABLE exercise_set IS 'Individual sets of an exercise';
COMMENT ON TABLE goal IS 'User fitness goals';
COMMENT ON TABLE workout_plan IS 'Pre-designed or custom workout plans';
COMMENT ON TABLE achievement IS 'Available achievements/badges';
COMMENT ON TABLE user_achievement IS 'User unlocked achievements';
COMMENT ON TABLE nutrition_log IS 'User nutrition/diet logs';
COMMENT ON TABLE user_statistics IS 'Aggregated user statistics';
COMMENT ON TABLE audit_log IS 'Audit trail for all user actions';

-- ==================== CONSTRAINTS SUMMARY ====================
-- All foreign keys have ON DELETE CASCADE for workout and exercise data
-- All foreign keys have ON DELETE CASCADE for user data (except audit_log which is ON DELETE SET NULL)
-- unique_user_achievement: users can only unlock an achievement once
-- All timestamp columns are updated automatically
-- Soft deletes are supported via deleted_at column where applicable