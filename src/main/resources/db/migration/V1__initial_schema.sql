-- Create UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create ENUM types
CREATE TYPE user_role AS ENUM ('USER', 'TRAINER', 'ADMIN');
CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'INACTIVE', 'EXPIRED', 'CANCELLED');
CREATE TYPE message_status AS ENUM ('UNREAD', 'READ', 'ARCHIVED');
CREATE TYPE exercise_difficulty AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED');
CREATE TYPE routine_day_type AS ENUM ('PUSH', 'PULL', 'LEGS', 'CARDIO', 'REST', 'FULLBODY', 'CUSTOM');

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    profile_picture_url TEXT,
    bio TEXT,
    role user_role NOT NULL DEFAULT 'USER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Trainers table
CREATE TABLE trainers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,
    specialization VARCHAR(100) NOT NULL,
    certification_number VARCHAR(100) UNIQUE NOT NULL,
    certification_file_url TEXT,
    years_of_experience INT NOT NULL,
    hourly_rate DECIMAL(10, 2),
    bio_professional TEXT,
    rating DECIMAL(3, 2) DEFAULT 0.0,
    total_reviews INT DEFAULT 0,
    is_verified BOOLEAN DEFAULT false,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_trainers_user_id ON trainers(user_id);
CREATE INDEX idx_trainers_is_verified ON trainers(is_verified);
CREATE INDEX idx_trainers_rating ON trainers(rating);

-- Gyms table
CREATE TABLE gyms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    website_url TEXT,
    logo_url TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    opening_time TIME,
    closing_time TIME,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_gyms_city ON gyms(city);
CREATE INDEX idx_gyms_is_active ON gyms(is_active);
CREATE INDEX idx_gyms_name ON gyms(name);

-- Subscriptions table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    trainer_id UUID,
    subscription_type VARCHAR(50) NOT NULL,
    status subscription_status DEFAULT 'INACTIVE',
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    price DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    auto_renew BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_gym_id ON subscriptions(gym_id);
CREATE INDEX idx_subscriptions_trainer_id ON subscriptions(trainer_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);

-- Muscle groups table
CREATE TABLE muscle_groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    anatomical_region VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_muscle_groups_name ON muscle_groups(name);

-- Exercises table
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    instructions TEXT,
    difficulty exercise_difficulty DEFAULT 'BEGINNER',
    image_url TEXT,
    video_url TEXT,
    equipment_required VARCHAR(200),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_exercises_name ON exercises(name);
CREATE INDEX idx_exercises_difficulty ON exercises(difficulty);
CREATE INDEX idx_exercises_is_active ON exercises(is_active);

-- Exercise muscle groups (many-to-many)
CREATE TABLE exercise_muscle_groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exercise_id UUID NOT NULL,
    muscle_group_id UUID NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (muscle_group_id) REFERENCES muscle_groups(id) ON DELETE CASCADE,
    UNIQUE(exercise_id, muscle_group_id)
);

CREATE INDEX idx_exercise_muscle_groups_exercise_id ON exercise_muscle_groups(exercise_id);
CREATE INDEX idx_exercise_muscle_groups_muscle_group_id ON exercise_muscle_groups(muscle_group_id);

-- Routines table
CREATE TABLE routines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    trainer_id UUID,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    goal VARCHAR(100),
    duration_weeks INT,
    is_active BOOLEAN DEFAULT true,
    is_public BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL
);

CREATE INDEX idx_routines_user_id ON routines(user_id);
CREATE INDEX idx_routines_trainer_id ON routines(trainer_id);
CREATE INDEX idx_routines_is_active ON routines(is_active);
CREATE INDEX idx_routines_is_public ON routines(is_public);

-- Routine days table
CREATE TABLE routine_days (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    routine_id UUID NOT NULL,
    day_number INT NOT NULL,
    day_name VARCHAR(20),
    day_type routine_day_type DEFAULT 'CUSTOM',
    description TEXT,
    rest_duration_minutes INT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE,
    UNIQUE(routine_id, day_number)
);

CREATE INDEX idx_routine_days_routine_id ON routine_days(routine_id);
CREATE INDEX idx_routine_days_day_type ON routine_days(day_type);

-- Routine exercises table
CREATE TABLE routine_exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    routine_day_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    sequence_number INT NOT NULL,
    sets INT NOT NULL DEFAULT 3,
    reps INT,
    reps_range_min INT,
    reps_range_max INT,
    weight DECIMAL(8, 2),
    duration_seconds INT,
    rest_seconds INT DEFAULT 60,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (routine_day_id) REFERENCES routine_days(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE RESTRICT,
    UNIQUE(routine_day_id, sequence_number)
);

CREATE INDEX idx_routine_exercises_routine_day_id ON routine_exercises(routine_day_id);
CREATE INDEX idx_routine_exercises_exercise_id ON routine_exercises(exercise_id);

-- Messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    subject VARCHAR(200),
    body TEXT NOT NULL,
    status message_status DEFAULT 'UNREAD',
    is_deleted_by_sender BOOLEAN DEFAULT false,
    is_deleted_by_receiver BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_receiver_status ON messages(receiver_id, status);

-- Audit trail for tracking changes
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    changed_by UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Insert default muscle groups
INSERT INTO muscle_groups (name, description, anatomical_region) VALUES
    ('Chest', 'Pectoral muscles', 'Upper Body'),
    ('Back', 'Latissimus dorsi and other back muscles', 'Upper Body'),
    ('Shoulders', 'Deltoids and surrounding muscles', 'Upper Body'),
    ('Biceps', 'Front arm muscles', 'Upper Body'),
    ('Triceps', 'Back arm muscles', 'Upper Body'),
    ('Forearms', 'Forearm muscles', 'Upper Body'),
    ('Abs', 'Abdominal muscles', 'Core'),
    ('Core', 'Core stabilizer muscles', 'Core'),
    ('Quadriceps', 'Front thigh muscles', 'Lower Body'),
    ('Hamstrings', 'Back thigh muscles', 'Lower Body'),
    ('Glutes', 'Gluteal muscles', 'Lower Body'),
    ('Calves', 'Calf muscles', 'Lower Body'),
    ('Legs', 'All leg muscles', 'Lower Body');
