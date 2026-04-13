package com.train.repository;

import com.train.domain.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {

    List<Session> findByUserId(String userId);

    List<Session> findByUserIdOrderByStartTimeDesc(String userId);

    List<Session> findByUserIdAndStartTimeBetween(
        String userId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0 } }",
        "{ $group: { _id: '$exerciseId', totalSessions: { $sum: 1 }, avgDuration: { $avg: '$durationMinutes' }, totalVolume: { $sum: '$totalVolume' } } }",
        "{ $sort: { totalSessions: -1 } }"
    })
    List<ExerciseStatsProjection> getExerciseStats(String userId);

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0, startTime: { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$startTime' } }, sessionCount: { $sum: 1 }, totalMinutes: { $sum: '$durationMinutes' }, avgIntensity: { $avg: '$intensity' } } }",
        "{ $sort: { _id: 1 } }"
    })
    List<DailyActivityProjection> getDailyActivity(String userId, LocalDateTime startDate, LocalDateTime endDate);

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0 } }",
        "{ $group: { _id: '$muscleGroup', count: { $sum: 1 }, avgIntensity: { $avg: '$intensity' } } }",
        "{ $sort: { count: -1 } }"
    })
    List<MuscleGroupStatsProjection> getMuscleGroupStats(String userId);

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0, startTime: { $gte: ?1 } } }",
        "{ $group: { _id: null, totalSessions: { $sum: 1 }, totalMinutes: { $sum: '$durationMinutes' }, avgIntensity: { $avg: '$intensity' }, maxIntensity: { $max: '$intensity' }, minIntensity: { $min: '$intensity' } } }"
    })
    List<UserStatsProjection> getUserStats(String userId, LocalDateTime fromDate);

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0, startTime: { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { _id: '$exerciseId', totalReps: { $sum: '$totalReps' }, totalWeight: { $sum: '$totalWeight' }, sessions: { $sum: 1 } } }",
        "{ $sort: { totalWeight: -1 } }",
        "{ $limit: 10 }"
    })
    List<TopExercisesProjection> getTopExercises(String userId, LocalDateTime startDate, LocalDateTime endDate);

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0 } }",
        "{ $group: { _id: '$muscleGroup', totalVolume: { $sum: '$totalVolume' } } }",
        "{ $sort: { totalVolume: -1 } }"
    })
    List<VolumeByMuscleProjection> getVolumeByMuscleGroup(String userId);

    long countByUserIdAndStartTimeAfter(String userId, LocalDateTime date);

    Optional<Session> findFirstByUserIdOrderByStartTimeDesc(String userId);
}
