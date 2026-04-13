package com.train.service;

import com.train.domain.Session;
import com.train.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
public class SessionAnalyticsService {

    @Autowired
    private SessionRepository sessionRepository;

    public List<ExerciseStatsProjection> getExerciseStats(String userId) {
        return sessionRepository.getExerciseStats(userId);
    }

    public List<DailyActivityProjection> getDailyActivityLastMonth(String userId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);
        return sessionRepository.getDailyActivity(userId, startDate, endDate);
    }

    public List<DailyActivityProjection> getDailyActivityCustomRange(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return sessionRepository.getDailyActivity(userId, startDate, endDate);
    }

    public List<MuscleGroupStatsProjection> getMuscleGroupStats(String userId) {
        return sessionRepository.getMuscleGroupStats(userId);
    }

    public List<UserStatsProjection> getUserStatsLastMonth(String userId) {
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        return sessionRepository.getUserStats(userId, lastMonth);
    }

    public List<UserStatsProjection> getUserStatsLastWeek(String userId) {
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        return sessionRepository.getUserStats(userId, lastWeek);
    }

    public List<TopExercisesProjection> getTopExercisesLastMonth(String userId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);
        return sessionRepository.getTopExercises(userId, startDate, endDate);
    }

    public List<TopExercisesProjection> getTopExercisesCustomRange(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return sessionRepository.getTopExercises(userId, startDate, endDate);
    }

    public List<VolumeByMuscleProjection> getVolumeByMuscleGroup(String userId) {
        return sessionRepository.getVolumeByMuscleGroup(userId);
    }

    public long getSessionCountLastWeek(String userId) {
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        return sessionRepository.countByUserIdAndStartTimeAfter(userId, lastWeek);
    }

    public long getSessionCountLastMonth(String userId) {
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        return sessionRepository.countByUserIdAndStartTimeAfter(userId, lastMonth);
    }

    public Optional<Session> getLastSession(String userId) {
        return sessionRepository.findFirstByUserIdOrderByStartTimeDesc(userId);
    }

    public List<Session> getUserSessionsLastMonth(String userId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);
        return sessionRepository.findByUserIdAndStartTimeBetween(userId, startDate, endDate);
    }
}
