package com.train.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "routine_exercises", indexes = {
    @Index(name = "idx_routine_exercises_routine_day_id", columnList = "routine_day_id"),
    @Index(name = "idx_routine_exercises_exercise_id", columnList = "exercise_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_day_id", nullable = false)
    private RoutineDay routineDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false)
    @Builder.Default
    private Integer sets = 3;

    @Column
    private Integer reps;

    @Column
    private Integer repsRangeMin;

    @Column
    private Integer repsRangeMax;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column
    private Integer durationSeconds;

    @Column
    @Builder.Default
    private Integer restSeconds = 60;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
