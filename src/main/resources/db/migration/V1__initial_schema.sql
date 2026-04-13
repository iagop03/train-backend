-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create muscle_groups table
CREATE TABLE muscle_groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_muscle_groups_name ON muscle_groups(name);

-- Create gyms table
CREATE TABLE gyms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone VARCHAR(20),
    email VARCHAR(150),
    website VARCHAR(255),
    logo_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_gyms_city ON gyms(city);
CREATE INDEX idx_gyms_is_active ON gyms(is_active);
CREATE INDEX idx_gyms_coordinates ON gyms(latitude, longitude);

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(20),
    profile_picture_url VARCHAR(500),
    bio TEXT,
    height_cm DECIMAL(5, 2),
    weight_kg DECIMAL(6, 2),
    gym_id UUID REFERENCES gyms(id) ON DELETE SET NULL,
    is_trainer BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_gym_id ON users(gym_id);
CREATE INDEX idx_users_is_trainer ON users(is_trainer);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Create trainers table
CREATE TABLE trainers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    specialization VARCHAR(200),
    certification_number VARCHAR(100) UNIQUE,
    certification_expiry_date DATE,
    bio TEXT,
    experience_years INT,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    hourly_rate DECIMAL(10, 2),
    is_available BOOLEAN DEFAULT TRUE,
    total_clients INT DEFAULT 0,
    gym_id UUID REFERENCES gyms(id) ON DELETE SET NULL,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trainers_user_id ON trainers(user_id);
CREATE INDEX idx_trainers_gym_id ON trainers(gym_id);
CREATE INDEX idx_trainers_is_available ON trainers(is_available);
CREATE INDEX idx_trainers_verified ON trainers(verified);
CREATE INDEX idx_trainers_rating ON trainers(rating DESC);

-- Create exercises table
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    muscle_group_id UUID NOT NULL REFERENCES muscle_groups(id) ON DELETE RESTRICT,
    equipment_required VARCHAR(200),
    difficulty_level VARCHAR(20),
    video_url VARCHAR(500),
    image_url VARCHAR(500),
    instructions TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exercises_name ON exercises(name);
CREATE INDEX idx_exercises_muscle_group_id ON exercises(muscle_group_id);
CREATE INDEX idx_exercises_difficulty_level ON exercises(difficulty_level);
CREATE INDEX idx_exercises_is_active ON exercises(is_active);

-- Create routines table
CREATE TABLE routines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    trainer_id UUID REFERENCES trainers(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    goal VARCHAR(100),
    difficulty_level VARCHAR(20),
    duration_weeks INT,
    is_template BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_routines_user_id ON routines(user_id);
CREATE INDEX idx_routines_trainer_id ON routines(trainer_id);
CREATE INDEX idx_routines_is_template ON routines(is_template);
CREATE INDEX idx_routines_is_active ON routines(is_active);
CREATE INDEX idx_routines_start_date ON routines(start_date);

-- Create routine_days table
CREATE TABLE routine_days (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    routine_id UUID NOT NULL REFERENCES routines(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL,
    day_name VARCHAR(20),
    is_rest_day BOOLEAN DEFAULT FALSE,
    notes TEXT,
    rest_duration_seconds INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(routine_id, day_of_week)
);

CREATE INDEX idx_routine_days_routine_id ON routine_days(routine_id);
CREATE INDEX idx_routine_days_day_of_week ON routine_days(day_of_week);

-- Create routine_exercises table
CREATE TABLE routine_exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    routine_day_id UUID NOT NULL REFERENCES routine_days(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    sets INT NOT NULL,
    reps INT NOT NULL,
    weight_kg DECIMAL(8, 2),
    duration_seconds INT,
    rest_seconds INT,
    notes TEXT,
    order_in_day INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_routine_exercises_routine_day_id ON routine_exercises(routine_day_id);
CREATE INDEX idx_routine_exercises_exercise_id ON routine_exercises(exercise_id);
CREATE INDEX idx_routine_exercises_order_in_day ON routine_exercises(routine_day_id, order_in_day);

-- Create subscriptions table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE RESTRICT,
    subscription_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    renewal_date TIMESTAMP,
    price DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'USD',
    payment_method VARCHAR(50),
    auto_renewal BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_gym_id ON subscriptions(gym_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_start_date ON subscriptions(start_date);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);

-- Create messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    message_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    parent_message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
    attachment_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX idx_messages_is_read ON messages(is_read);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
CREATE INDEX idx_messages_conversation ON messages(sender_id, recipient_id);

-- Create audit trail table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    action VARCHAR(50),
    changes JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- Create user_followers table (for social features)
CREATE TABLE user_followers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(follower_id, following_id),
    CHECK (follower_id != following_id)
);

CREATE INDEX idx_user_followers_follower_id ON user_followers(follower_id);
CREATE INDEX idx_user_followers_following_id ON user_followers(following_id);

-- Create trainer_clients table (many-to-many relationship)
CREATE TABLE trainer_clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trainer_id UUID NOT NULL REFERENCES trainers(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(trainer_id, client_id)
);

CREATE INDEX idx_trainer_clients_trainer_id ON trainer_clients(trainer_id);
CREATE INDEX idx_trainer_clients_client_id ON trainer_clients(client_id);
CREATE INDEX idx_trainer_clients_status ON trainer_clients(status);

-- Create views for common queries

-- View: Active users
CREATE VIEW active_users AS
SELECT * FROM users WHERE is_active = TRUE;

-- View: Active trainers with stats
CREATE VIEW trainer_statistics AS
SELECT 
    t.id,
    t.user_id,
    u.first_name,
    u.last_name,
    u.email,
    t.specialization,
    t.rating,
    t.experience_years,
    COUNT(DISTINCT tc.client_id) as active_clients,
    COUNT(DISTINCT r.id) as total_routines,
    t.created_at
FROM trainers t
JOIN users u ON t.user_id = u.id
LEFT JOIN trainer_clients tc ON t.id = tc.trainer_id AND tc.status = 'ACTIVE'
LEFT JOIN routines r ON t.id = r.trainer_id AND r.is_active = TRUE
WHERE t.verified = TRUE
GROUP BY t.id, t.user_id, u.first_name, u.last_name, u.email, t.specialization, t.rating, t.experience_years, t.created_at;

-- View: User routines overview
CREATE VIEW user_routine_overview AS
SELECT 
    r.id,
    r.user_id,
    r.name,
    r.goal,
    r.difficulty_level,
    COUNT(DISTINCT rd.id) as total_days,
    COUNT(DISTINCT re.id) as total_exercises,
    r.start_date,
    r.end_date,
    r.is_active
FROM routines r
LEFT JOIN routine_days rd ON r.id = rd.routine_id
LEFT JOIN routine_exercises re ON rd.id = re.routine_day_id
GROUP BY r.id, r.user_id, r.name, r.goal, r.difficulty_level, r.start_date, r.end_date, r.is_active;