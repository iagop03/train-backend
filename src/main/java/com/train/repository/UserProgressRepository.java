package com.train.repository;

import com.train.model.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends MongoRepository<UserProgress, String> {
    
    // Find by user_id
    List<UserProgress> findByUserId(String userId);
    
    // Find by user_id and date range
    @Query("{ 'user_id': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<UserProgress> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by user_id and workout type
    @Query("{ 'user_id': ?0, 'workout_type': ?1 }")
    List<UserProgress> findByUserIdAndWorkoutType(String userId, String workoutType);
    
    // Find latest workout by user_id
    @Query("{ 'user_id': ?0 }")
    Optional<UserProgress> findLatestByUserId(String userId);
    
    // Find by user_id ordered by date descending
    List<UserProgress> findByUserIdOrderByDateDesc(String userId);
    
    // Aggregation: Weekly volume and stats
    @Aggregation(pipeline = {
        "{ $match: { 'user_id': ?0, 'date': { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { _id: { $week: '$date' }, totalVolume: { $sum: '$total_volume' }, totalReps: { $sum: '$total_reps' }, totalSets: { $sum: '$total_sets' }, workoutCount: { $sum: 1 }, avgDuration: { $avg: '$duration_minutes' } } }",
        "{ $sort: { _id: -1 } }"
    })
    List<WeeklyVolumeDto> aggregateWeeklyVolume(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Aggregation: Progress over time (grouped by date)
    @Aggregation(pipeline = {
        "{ $match: { 'user_id': ?0, 'date': { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$date' } }, totalVolume: { $sum: '$total_volume' }, totalReps: { $sum: '$total_reps' }, workoutCount: { $sum: 1 } } }",
        "{ $sort: { _id: 1 } }"
    })
    List<DailyProgressDto> aggregateDailyProgress(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Aggregation: Workout type breakdown
    @Aggregation(pipeline = {
        "{ $match: { 'user_id': ?0, 'date': { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { _id: '$workout_type', totalVolume: { $sum: '$total_volume' }, totalReps: { $sum: '$total_reps' }, count: { $sum: 1 }, avgVolume: { $avg: '$total_volume' } } }",
        "{ $sort: { totalVolume: -1 } }"
    })
    List<WorkoutTypeBreakdownDto> aggregateWorkoutTypeBreakdown(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Aggregation: Top performing exercises
    @Aggregation(pipeline = {
        "{ $match: { 'user_id': ?0, 'date': { $gte: ?1, $lte: ?2 } } }",
        "{ $unwind: '$exercises_completed' }",
        "{ $group: { _id: '$exercises_completed.exercise_name', totalVolume: { $sum: '$exercises_completed.volume_lifted' }, totalReps: { $sum: '$exercises_completed.reps' }, count: { $sum: 1 } } }",
        "{ $sort: { totalVolume: -1 } }",
        "{ $limit: 10 }"
    })
    List<TopExerciseDto> aggregateTopExercises(String userId, LocalDateTime startDate, LocalDateTime endDate);
}
