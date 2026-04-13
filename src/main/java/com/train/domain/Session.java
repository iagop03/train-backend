package com.train.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sessions")
public class Session {

    @Id
    private String id;

    @Field("userId")
    private String userId;

    @Field("sessionName")
    private String sessionName;

    @Field("startTime")
    private LocalDateTime startTime;

    @Field("endTime")
    private LocalDateTime endTime;

    @Field("durationMinutes")
    private Integer durationMinutes;

    @Field("exercises")
    private List<ExerciseSession> exercises;

    @Field("totalVolume")
    private Double totalVolume;

    @Field("totalReps")
    private Integer totalReps;

    @Field("totalWeight")
    private Double totalWeight;

    @Field("intensity")
    private Integer intensity; // 1-10 scale

    @Field("muscleGroup")
    private String muscleGroup;

    @Field("notes")
    private String notes;

    @Field("completed")
    private Boolean completed;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseSession {
        @Field("exerciseId")
        private String exerciseId;

        @Field("exerciseName")
        private String exerciseName;

        @Field("muscleGroup")
        private String muscleGroup;

        @Field("sets")
        private List<SetData> sets;

        @Field("totalReps")
        private Integer totalReps;

        @Field("totalWeight")
        private Double totalWeight;

        @Field("totalVolume")
        private Double totalVolume;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SetData {
        @Field("setNumber")
        private Integer setNumber;

        @Field("reps")
        private Integer reps;

        @Field("weight")
        private Double weight;

        @Field("volume")
        private Double volume;

        @Field("rpe")
        private Integer rpe; // Rate of Perceived Exertion
    }
}
