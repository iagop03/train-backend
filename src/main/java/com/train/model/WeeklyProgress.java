package com.train.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyProgress {
    
    private String userId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Double totalWeeklyVolume;
    private Integer totalWeeklyReps;
    private Integer totalWeeklySets;
    private Integer totalDurationMinutes;
    private Integer workoutsCount;
    private Map<String, DailyStats> dailyStats;
    private Map<String, WorkoutTypeStats> workoutTypeStats;
    private Double volumeGrowthPercentage;
    private Integer averageWorkoutDuration;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStats {
        private LocalDate date;
        private Double dailyVolume;
        private Integer dailyReps;
        private Integer dailySets;
        private Integer durationMinutes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutTypeStats {
        private String workoutType;
        private Integer count;
        private Double totalVolume;
        private Integer totalReps;
    }
}
