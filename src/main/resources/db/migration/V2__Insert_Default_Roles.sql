-- V2__Insert_Default_Roles.sql
-- Insert default system roles
-- Created: 2024-01-15

INSERT INTO role (id, name, description, is_system, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'ADMIN', 'Administrator - Full system access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'TRAINER', 'Trainer - Can create plans and track client workouts', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440003', 'USER', 'User - Basic access to track own workouts', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440004', 'MODERATOR', 'Moderator - Can moderate content and user behavior', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440005', 'NUTRITIONIST', 'Nutritionist - Can create meal plans and manage nutrition', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;