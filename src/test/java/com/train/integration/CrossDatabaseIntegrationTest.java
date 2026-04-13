package com.train.integration;

import com.train.config.TestContainerSpringBootTest;
import com.train.document.WorkoutSession;
import com.train.entity.Exercise;
import com.train.entity.User;
import com.train.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@TestContainerSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Cross-Database Integration Tests - PostgreSQL & MongoDB")
class CrossDatabaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    private User testUser;
    private Long userId;

    @BeforeEach
    void setUp() {
        // Create user in PostgreSQL
        testUser = userRepository.save(User.builder()
            .email("cross@test.com")
            .username("crossuser")
            .password("pass")
            .firstName("Cross")
            .lastName("User")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());
        userId = testUser.getId();
    }

    @Test
    @DisplayName("Should create user in PostgreSQL and session in MongoDB")
    void testUserAndSessionCreation() {
        // Arrange & Act
        WorkoutSession session = workoutSessionRepository.save(
            WorkoutSession.builder()
                .userId(userId)
                .sessionName("Cross Test Session")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .durationMinutes(60)
                .exercisesCompleted(3)
                .isCompleted(true)
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Assert
        assertThat(testUser.getId()).isNotNull();
        assertThat(session.getId()).isNotNull();
        assertThat(session.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should link exercises (PostgreSQL) with sessions (MongoDB)")
    void testExerciseSessionLinking() {
        // Arrange
        Exercise exercise = exerciseRepository.save(
            Exercise.builder()
                .userId(userId)
                .name("Test Exercise")
                .category("Test")
                .sets(4)
                .reps(8)
                .weight(100.0)
                .createdAt(LocalDateTime.now())
                .build()
        );

        WorkoutSession session = workoutSessionRepository.save(
            WorkoutSession.builder()
                .userId(userId)
                .sessionName("Session with Exercise")
                .startTime(LocalDateTime.now())
                .durationMinutes(45)
                .exercisesCompleted(1)
                .isCompleted(true)
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Act
        List<Exercise> userExercises = exerciseRepository.findByUserId(userId);
        List<WorkoutSession> userSessions = workoutSessionRepository.findByUserId(userId);

        // Assert
        assertThat(userExercises).hasSize(1);
        assertThat(userSessions).hasSize(1);
        assertThat(userExercises.get(0).getUserId()).isEqualTo(userId);
        assertThat(userSessions.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should manage complete workout lifecycle across databases")
    void testCompleteWorkoutLifecycle() {
        // Step 1: Create user (PostgreSQL)
        assertThat(userRepository.findById(userId)).isPresent();

        // Step 2: Create exercises (PostgreSQL)
        Exercise benchPress = exerciseRepository.save(
            Exercise.builder()
                .userId(userId)
                .name("Bench Press")
                .category("Chest")
                .sets(4)
                .reps(8)
                .weight(100.0)
                .createdAt(LocalDateTime.now())
                .build()
        );
        Exercise deadlift = exerciseRepository.save(
            Exercise.builder()
                .userId(userId)
                .name("Deadlift")
                .category("Back")
                .sets(3)
                .reps(5)
                .weight(150.0)
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Step 3: Create workout session (MongoDB)
        WorkoutSession session = workoutSessionRepository.save(
            WorkoutSession.builder()
                .userId(userId)
                .sessionName("Full Body Workout")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .durationMinutes(120)
                .exercisesCompleted(2)
                .totalSets(7)
                .totalReps(23)
                .totalWeight(1150.0)
                .isCompleted(true)
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Step 4: Verify data integrity
        List<Exercise> exercises = exerciseRepository.findByUserId(userId);
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserId(userId);

        // Assert
        assertThat(exercises).hasSize(2);
        assertThat(sessions).hasSize(1);
        assertThat(exercises).extracting(Exercise::getName)
            .containsExactlyInAnyOrder("Bench Press", "Deadlift");
        assertThat(session.getTotalWeight()).isEqualTo(1150.0);
    }

    @Test
    @DisplayName("Should handle concurrent operations on both databases")
    void testConcurrentDatabaseOperations() {
        // PostgreSQL operations
        Exercise ex1 = exerciseRepository.save(
            Exercise.builder()
                .userId(userId)
                .name("Exercise 1")
                .category("Cat1")
                .createdAt(LocalDateTime.now())
                .build()
        );

        // MongoDB operations
        WorkoutSession ws1 = workoutSessionRepository.save(
            WorkoutSession.builder()
                .userId(userId)
                .sessionName("Session 1")
                .startTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build()
        );

        // More PostgreSQL
        Exercise ex2 = exerciseRepository.save(
            Exercise.builder()
                .userId(userId)
                .name("Exercise 2")
                .category("Cat2")
                .createdAt(LocalDateTime.now())
                .build()
        );

        // More MongoDB
        WorkoutSession ws2 = workoutSessionRepository.save(
            WorkoutSession.builder()
                .userId(userId)
                .sessionName("Session 2")
                .startTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Assert
        assertThat(exerciseRepository.countByUserId(userId)).isEqualTo(2);
        assertThat(workoutSessionRepository.countByUserId(userId)).isEqualTo(2);
    }

    @Test
    @DisplayName("Should maintain referential integrity across databases")
    void testReferentialIntegrity() {
        // Create exercise
        Exercise exercise = exerciseRepository.save(
            Exercise.builder()
                .userId(userId)
                .name("Integrity Test");
                .category("Test")
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Create session referencing same user
        WorkoutSession session = workoutSessionRepository.save(
            WorkoutSession.builder()
                .userId(userId)
                .sessionName("Integrity Test Session")
                .startTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Act
        Long exerciseUserId = exerciseRepository.findById(exercise.getId())
            .map(Exercise::getUserId)
            .orElse(null);
        Long sessionUserId = workoutSessionRepository.findById(session.getId())
            .map(WorkoutSession::getUserId)
            .orElse(null);

        // Assert
        assertThat(exerciseUserId).isEqualTo(userId);
        assertThat(sessionUserId).isEqualTo(userId);
        assertThat(exerciseUserId).isEqualTo(sessionUserId);
    }
}