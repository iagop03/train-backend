package com.train.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "user_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {
    
    @Id
    private String id;
    
    @Field("user_id")
    private String userId;
    
    @Field("date")
    private LocalDateTime date;
    
    @Field("workout_type")
    private String workoutType;
    
    @Field("total_volume")
    private Double totalVolume;
    
    @Field("total_reps")
    private Integer totalReps;
    
    @Field("total_sets")
    private Integer totalSets;
    
    @Field("duration_minutes")
    private Integer durationMinutes;
    
    @Field("exercises_completed")
    private List<ExerciseData> exercisesCompleted;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseData {
        private String exerciseId;
        private String exerciseName;
        private Integer reps;
        private Integer sets;
        private Double weight;
        private Double volumeLifted;
    }
}
