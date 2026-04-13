package com.train.service;

import com.train.model.UserProgress;
import com.train.model.WeeklyProgress;
import com.train.repository.UserProgressRepository;
import com.train.repository.dto.WeeklyVolumeDto;
import com.train.repository.dto.DailyProgressDto;
import com.train.repository.dto.WorkoutTypeBreakdownDto;
import com.train.repository.dto.TopExerciseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProgressService {
    
    @Autowired
    private UserProgressRepository progressRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Save user progress
     */
    public UserProgress saveProgress(UserProgress progress) {
        progress.setCreatedAt(LocalDateTime.now());
        progress.setUpdatedAt(LocalDateTime.now());
        log.info("Saving progress for user: {}", progress.getUserId());
        return progressRepository.save(progress);
    }
    
    /**
     * Get all progress records for a user
     */
    public List<UserProgress> getUserProgress(String userId) {
        log.info("Fetching progress for user: {}", userId);
        return progressRepository.findByUserIdOrderByDateDesc(userId);
    }
    
    /**
     * Get progress for user in date range
     */
    public List<UserProgress> getUserProgressByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching progress for user: {} between {} and {}", userId, startDate, endDate);
        return progressRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * Get weekly aggregated progress
     */
    public WeeklyProgress getWeeklyProgress(String userId, LocalDate weekDate) {
        log.info("Calculating weekly progress for user: {} for week of {}", userId, weekDate);
        
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate weekStart = weekDate.with(weekFields.dayOfWeek(), 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);
        
        List<UserProgress> weeklyRecords = progressRepository.findByUserIdAndDateRange(userId, startDateTime, endDateTime);
        
        if (weeklyRecords.isEmpty()) {
            log.warn("No progress records found for user: {} in week {}", userId, weekDate);
            return new WeeklyProgress();
        }
        
        // Calculate aggregated stats
        Double totalVolume = weeklyRecords.stream()
            .mapToDouble(p -> p.getTotalVolume() != null ? p.getTotalVolume() : 0)
            .sum();
        
        Integer totalReps = weeklyRecords.stream()
            .mapToInt(p -> p.getTotalReps() != null ? p.getTotalReps() : 0)
            .sum();
        
        Integer totalSets = weeklyRecords.stream()
            .mapToInt(p -> p.getTotalSets() != null ? p.getTotalSets() : 0)
            .sum();
        
        Integer totalDuration = weeklyRecords.stream()
            .mapToInt(p -> p.getDurationMinutes() != null ? p.getDurationMinutes() : 0)
            .sum();
        
        // Daily stats
        Map<String, WeeklyProgress.DailyStats> dailyStats = weeklyRecords.stream()
            .collect(Collectors.toMap(
                p -> p.getDate().toLocalDate().toString(),
                p -> new WeeklyProgress.DailyStats(
                    p.getDate().toLocalDate(),
                    p.getTotalVolume(),
                    p.getTotalReps(),
                    p.getTotalSets(),
                    p.getDurationMinutes()
                ),
                (existing, replacement) -> {
                    existing.setDailyVolume(existing.getDailyVolume() + replacement.getDailyVolume());
                    existing.setDailyReps(existing.getDailyReps() + replacement.getDailyReps());
                    existing.setDailySets(existing.getDailySets() + replacement.getDailySets());
                    existing.setDurationMinutes(existing.getDurationMinutes() + replacement.getDurationMinutes());
                    return existing;
                }
            ));
        
        // Workout type stats
        Map<String, WeeklyProgress.WorkoutTypeStats> workoutTypeStats = weeklyRecords.stream()
            .collect(Collectors.toMap(
                UserProgress::getWorkoutType,
                p -> new WeeklyProgress.WorkoutTypeStats(
                    p.getWorkoutType(),
                    1,
                    p.getTotalVolume(),
                    p.getTotalReps()
                ),
                (existing, replacement) -> {
                    existing.setCount(existing.getCount() + 1);
                    existing.setTotalVolume(existing.getTotalVolume() + replacement.getTotalVolume());
                    existing.setTotalReps(existing.getTotalReps() + replacement.getTotalReps());
                    return existing;
                }
            ));
        
        Integer avgDuration = totalDuration / weeklyRecords.size();
        
        WeeklyProgress weekly = new WeeklyProgress();
        weekly.setUserId(userId);
        weekly.setWeekStart(weekStart);
        weekly.setWeekEnd(weekEnd);
        weekly.setTotalWeeklyVolume(totalVolume);
        weekly.setTotalWeeklyReps(totalReps);
        weekly.setTotalWeeklySets(totalSets);
        weekly.setTotalDurationMinutes(totalDuration);
        weekly.setWorkoutsCount(weeklyRecords.size());
        weekly.setDailyStats(dailyStats);
        weekly.setWorkoutTypeStats(workoutTypeStats);
        weekly.setAverageWorkoutDuration(avgDuration);
        
        log.info("Weekly progress calculated for user: {} - Total Volume: {}", userId, totalVolume);
        return weekly;
    }
    
    /**
     * Get weekly volume aggregation using MongoDB pipeline
     */
    public List<WeeklyVolumeDto> getWeeklyVolumeAggregation(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting weekly volume aggregation for user: {}", userId);
        return progressRepository.aggregateWeeklyVolume(userId, startDate, endDate);
    }
    
    /**
     * Get daily progress trend
     */
    public List<DailyProgressDto> getDailyProgressTrend(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting daily progress trend for user: {}", userId);
        return progressRepository.aggregateDailyProgress(userId, startDate, endDate);
    }
    
    /**
     * Get workout type breakdown
     */
    public List<WorkoutTypeBreakdownDto> getWorkoutTypeBreakdown(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting workout type breakdown for user: {}", userId);
        return progressRepository.aggregateWorkoutTypeBreakdown(userId, startDate, endDate);
    }
    
    /**
     * Get top performing exercises
     */
    public List<TopExerciseDto> getTopExercises(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting top exercises for user: {}", userId);
        return progressRepository.aggregateTopExercises(userId, startDate, endDate);
    }
    
    /**
     * Calculate volume growth percentage
     */
    public Double calculateVolumeGrowth(String userId, LocalDate currentWeek, LocalDate previousWeek) {
        log.info("Calculating volume growth for user: {} between {} and {}", userId, previousWeek, currentWeek);
        
        WeeklyProgress currentProgress = getWeeklyProgress(userId, currentWeek);
        WeeklyProgress previousProgress = getWeeklyProgress(userId, previousWeek);
        
        if (previousProgress.getTotalWeeklyVolume() == null || previousProgress.getTotalWeeklyVolume() == 0) {
            return 0.0;
        }
        
        double growth = ((currentProgress.getTotalWeeklyVolume() - previousProgress.getTotalWeeklyVolume()) 
            / previousProgress.getTotalWeeklyVolume()) * 100;
        
        log.info("Volume growth calculated: {}%", growth);
        return growth;
    }
    
    /**
     * Get progress statistics for last N days
     */
    public Map<String, Object> getProgressStats(String userId, Integer days) {
        log.info("Getting progress stats for user: {} last {} days", userId, days);
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<UserProgress> progressRecords = getUserProgressByDateRange(userId, startDate, endDate);
        
        if (progressRecords.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWorkouts", progressRecords.size());
        stats.put("totalVolume", progressRecords.stream()
            .mapToDouble(p -> p.getTotalVolume() != null ? p.getTotalVolume() : 0)
            .sum());
        stats.put("totalReps", progressRecords.stream()
            .mapToInt(p -> p.getTotalReps() != null ? p.getTotalReps() : 0)
            .sum());
        stats.put("totalDuration", progressRecords.stream()
            .mapToInt(p -> p.getDurationMinutes() != null ? p.getDurationMinutes() : 0)
            .sum());
        stats.put("avgDuration", progressRecords.stream()
            .mapToInt(p -> p.getDurationMinutes() != null ? p.getDurationMinutes() : 0)
            .average()
            .orElse(0.0));
        stats.put("days", days);
        
        return stats;
    }
}
