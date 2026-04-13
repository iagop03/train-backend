-- V3__Insert_Sample_Achievements.sql
-- Insert sample achievements/badges
-- Created: 2024-01-15

INSERT INTO achievement (id, name, description, icon_url, badge_level, criteria_type, criteria_value, created_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655450001', 'First Workout', 'Complete your first workout', '/icons/first-workout.png', 'BRONZE', 'WORKOUTS_COMPLETED', 1, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450002', 'Warrior', 'Complete 10 workouts', '/icons/warrior.png', 'BRONZE', 'WORKOUTS_COMPLETED', 10, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450003', 'Champion', 'Complete 50 workouts', '/icons/champion.png', 'SILVER', 'WORKOUTS_COMPLETED', 50, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450004', 'Legend', 'Complete 100 workouts', '/icons/legend.png', 'GOLD', 'WORKOUTS_COMPLETED', 100, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450005', 'Immortal', 'Complete 250 workouts', '/icons/immortal.png', 'PLATINUM', 'WORKOUTS_COMPLETED', 250, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450006', 'Week Warrior', 'Maintain a 7-day workout streak', '/icons/week-warrior.png', 'BRONZE', 'STREAK_DAYS', 7, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450007', 'Unstoppable', 'Maintain a 30-day workout streak', '/icons/unstoppable.png', 'SILVER', 'STREAK_DAYS', 30, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450008', 'Goal Setter', 'Create your first goal', '/icons/goal-setter.png', 'BRONZE', 'GOALS_CREATED', 1, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450009', 'Goal Master', 'Complete 5 goals', '/icons/goal-master.png', 'SILVER', 'GOALS_COMPLETED', 5, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450010', 'Social Butterfly', 'Get 10 followers', '/icons/social-butterfly.png', 'BRONZE', 'FOLLOWERS', 10, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450011', 'Influencer', 'Get 100 followers', '/icons/influencer.png', 'GOLD', 'FOLLOWERS', 100, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450012', 'Calorie Crusher', 'Burn 10,000 calories total', '/icons/calorie-crusher.png', 'SILVER', 'CALORIES_BURNED', 10000, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655450013', 'Inferno', 'Burn 100,000 calories total', '/icons/inferno.png', 'GOLD', 'CALORIES_BURNED', 100000, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;