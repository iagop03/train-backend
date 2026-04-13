package com.train.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Workout entity mapped to the 'workout' table.
 * Represents a single workout session for a user.
 */
@Entity
@Table(name = "workout", indexes = {
        @Index(name = "idx_workout_user_id", columnList = "user_id"),
        @Index(name = "idx_workout_start_time", columnList = "start_time"),
        @Index(name = "idx_workout_created_at", columnList = "created_at"),
        @Index(name = "idx_workout_is_completed", columnList = "is_completed"),
        @Index(name = "idx_workout_user_start_time", columnList = "user_id,start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "workout_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private WorkoutType workoutType;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "intensity_level", length = 20)
    @Enumerated(EnumType.STRING)
    private IntensityLevel intensityLevel;

    @Column(name = "calories_burned")
    private Double caloriesBurned;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();

    public enum WorkoutType {
        STRENGTH, CARDIO, FLEXIBILITY, SPORTS, CUSTOM
    }

    public enum IntensityLevel {
        LOW, MODERATE, HIGH, VERY_HIGH
    }

    public void markAsCompleted() {
        this.isCompleted = true;
        this.endTime = LocalDateTime.now();
    }
}
