-- Create workout_sessions table for tracking user workouts
CREATE TABLE workout_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    routine_id UUID REFERENCES routines(id) ON DELETE SET NULL,
    routine_day_id UUID REFERENCES routine_days(id) ON DELETE SET NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INT,
    notes TEXT,
    mood_before INT,
    mood_after INT,
    calories_burned DECIMAL(8, 2),
    intensity_level INT,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workout_sessions_user_id ON workout_sessions(user_id);
CREATE INDEX idx_workout_sessions_routine_id ON workout_sessions(routine_id);
CREATE INDEX idx_workout_sessions_start_time ON workout_sessions(start_time DESC);
CREATE INDEX idx_workout_sessions_is_completed ON workout_sessions(is_completed);

-- Create exercise_logs table for tracking individual exercise performance
CREATE TABLE exercise_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_session_id UUID NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    routine_exercise_id UUID NOT NULL REFERENCES routine_exercises(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    sets_completed INT,
    reps_completed INT,
    weight_used_kg DECIMAL(8, 2),
    duration_seconds INT,
    rest_taken_seconds INT,
    difficulty_rating INT,
    notes TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exercise_logs_workout_session_id ON exercise_logs(workout_session_id);
CREATE INDEX idx_exercise_logs_exercise_id ON exercise_logs(exercise_id);
CREATE INDEX idx_exercise_logs_is_completed ON exercise_logs(is_completed);

-- Create personal_records table
CREATE TABLE personal_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    weight_kg DECIMAL(8, 2),
    reps INT,
    date_achieved DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, exercise_id)
);

CREATE INDEX idx_personal_records_user_id ON personal_records(user_id);
CREATE INDEX idx_personal_records_exercise_id ON personal_records(exercise_id);
CREATE INDEX idx_personal_records_date_achieved ON personal_records(date_achieved DESC);

-- Create body_measurements table
CREATE TABLE body_measurements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    measurement_date DATE NOT NULL,
    weight_kg DECIMAL(6, 2),
    body_fat_percentage DECIMAL(5, 2),
    chest_cm DECIMAL(6, 2),
    waist_cm DECIMAL(6, 2),
    hip_cm DECIMAL(6, 2),
    bicep_cm DECIMAL(6, 2),
    thigh_cm DECIMAL(6, 2),
    calf_cm DECIMAL(6, 2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_body_measurements_user_id ON body_measurements(user_id);
CREATE INDEX idx_body_measurements_measurement_date ON body_measurements(measurement_date DESC);

-- Create view for user progress
CREATE VIEW user_progress_summary AS
SELECT 
    u.id,
    u.first_name,
    u.last_name,
    COUNT(DISTINCT ws.id) as total_workouts,
    COUNT(DISTINCT CASE WHEN ws.is_completed = TRUE THEN ws.id END) as completed_workouts,
    COALESCE(SUM(ws.duration_minutes), 0) as total_minutes,
    COALESCE(AVG(ws.intensity_level), 0) as avg_intensity,
    (SELECT weight_kg FROM body_measurements WHERE user_id = u.id ORDER BY measurement_date DESC LIMIT 1) as current_weight,
    (SELECT weight_kg FROM body_measurements WHERE user_id = u.id ORDER BY measurement_date ASC LIMIT 1) as starting_weight,
    COUNT(DISTINCT pr.id) as total_personal_records
FROM users u
LEFT JOIN workout_sessions ws ON u.id = ws.user_id
LEFT JOIN personal_records pr ON u.id = pr.user_id
GROUP BY u.id, u.first_name, u.last_name;