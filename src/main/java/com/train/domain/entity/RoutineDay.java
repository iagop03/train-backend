package com.train.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "routine_days", indexes = {
    @Index(name = "idx_routine_days_routine_id", columnList = "routine_id"),
    @Index(name = "idx_routine_days_day_type", columnList = "day_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private Integer dayNumber;

    @Column(length = 20)
    private String dayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoutineDayType dayType = RoutineDayType.CUSTOM;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer restDurationMinutes;

    @OneToMany(mappedBy = "routineDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoutineExercise> routineExercises;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum RoutineDayType {
        PUSH, PULL, LEGS, CARDIO, REST, FULLBODY, CUSTOM
    }
}
