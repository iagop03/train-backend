package com.train.repository;

import com.train.config.TestContainerSpringBootTest;
import com.train.document.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@TestContainerSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Integration Tests - WorkoutSessionRepository with MongoDB")
class WorkoutSessionRepositoryIntegrationTest {

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private WorkoutSession testSession;
    private Long userId;

    @BeforeEach
    void setUp() {
        // Create test user
        User user = userRepository.save(User.builder()
            .email("workout@test.com")
            .username("workoutuser")
            .password("pass")
            .firstName("Work")
            .lastName("Out")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());
        userId = user.getId();

        testSession = WorkoutSession.builder()
            .userId(userId)
            .sessionName("Chest Day")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now().plusHours(1))
            .durationMinutes(60)
            .exercisesCompleted(4)
            .totalSets(12)
            .totalReps(48)
            .totalWeight(1200.0)
            .notes("Great workout")
            .isCompleted(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save workout session to MongoDB successfully")
    void testSaveWorkoutSession() {
        // Act
        WorkoutSession savedSession = workoutSessionRepository.save(testSession);

        // Assert
        assertThat(savedSession).isNotNull();
        assertThat(savedSession.getId()).isNotNull();
        assertThat(savedSession.getSessionName()).isEqualTo("Chest Day");
        assertThat(savedSession.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should find workout sessions by user ID")
    void testFindSessionsByUserId() {
        // Arrange
        workoutSessionRepository.save(testSession);
        WorkoutSession session2 = WorkoutSession.builder()
            .userId(userId)
            .sessionName("Back Day")
            .startTime(LocalDateTime.now().minusDays(1))
            .endTime(LocalDateTime.now().minusDays(1).plusHours(1))
            .durationMinutes(60)
            .exercisesCompleted(5)
            .totalSets(15)
            .totalReps(60)
            .totalWeight(1500.0)
            .notes("Good session")
            .isCompleted(true)
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
        workoutSessionRepository.save(session2);

        // Act
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserId(userId);

        // Assert
        assertThat(sessions).hasSize(2);
        assertThat(sessions).extracting(WorkoutSession::getSessionName)
            .containsExactlyInAnyOrder("Chest Day", "Back Day");
    }

    @Test
    @DisplayName("Should find completed workout sessions")
    void testFindCompletedSessions() {
        // Arrange
        workoutSessionRepository.save(testSession);
        WorkoutSession incompleteSession = WorkoutSession.builder()
            .userId(userId)
            .sessionName("Incomplete Session")
            .startTime(LocalDateTime.now())
            .durationMinutes(30)
            .exercisesCompleted(2)
            .isCompleted(false)
            .createdAt(LocalDateTime.now())
            .build();
        workoutSessionRepository.save(incompleteSession);

        // Act
        List<WorkoutSession> completedSessions = workoutSessionRepository.findByIsCompleted(true);

        // Assert
        assertThat(completedSessions).hasSize(1);
        assertThat(completedSessions.get(0).getSessionName()).isEqualTo("Chest Day");
    }

    @Test
    @DisplayName("Should find workout session by ID")
    void testFindSessionById() {
        // Arrange
        WorkoutSession savedSession = workoutSessionRepository.save(testSession);

        // Act
        Optional<WorkoutSession> foundSession = workoutSessionRepository.findById(savedSession.getId());

        // Assert
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getSessionName()).isEqualTo("Chest Day");
    }

    @Test
    @DisplayName("Should update workout session")
    void testUpdateWorkoutSession() {
        // Arrange
        WorkoutSession savedSession = workoutSessionRepository.save(testSession);

        // Act
        savedSession.setNotes("Updated notes");
        savedSession.setTotalWeight(1300.0);
        WorkoutSession updatedSession = workoutSessionRepository.save(savedSession);

        // Assert
        assertThat(updatedSession.getNotes()).isEqualTo("Updated notes");
        assertThat(updatedSession.getTotalWeight()).isEqualTo(1300.0);
    }

    @Test
    @DisplayName("Should delete workout session")
    void testDeleteWorkoutSession() {
        // Arrange
        WorkoutSession savedSession = workoutSessionRepository.save(testSession);
        String sessionId = savedSession.getId();

        // Act
        workoutSessionRepository.deleteById(sessionId);
        Optional<WorkoutSession> deletedSession = workoutSessionRepository.findById(sessionId);

        // Assert
        assertThat(deletedSession).isEmpty();
    }

    @Test
    @DisplayName("Should find sessions by date range")
    void testFindSessionsByDateRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        workoutSessionRepository.save(testSession);
        
        WorkoutSession oldSession = WorkoutSession.builder()
            .userId(userId)
            .sessionName("Old Session")
            .startTime(now.minusDays(10))
            .endTime(now.minusDays(10).plusHours(1))
            .durationMinutes(60)
            .exercisesCompleted(3)
            .isCompleted(true)
            .createdAt(now.minusDays(10))
            .build();
        workoutSessionRepository.save(oldSession);

        // Act
        List<WorkoutSession> recentSessions = workoutSessionRepository
            .findByUserIdAndCreatedAtBetween(userId, now.minusDays(5), now.plusDays(1));

        // Assert
        assertThat(recentSessions).hasSize(1);
        assertThat(recentSessions.get(0).getSessionName()).isEqualTo("Chest Day");
    }

    @Test
    @DisplayName("Should count sessions by user")
    void testCountSessionsByUser() {
        // Arrange
        workoutSessionRepository.save(testSession);
        workoutSessionRepository.save(testSession);
        workoutSessionRepository.save(testSession);

        // Act
        long count = workoutSessionRepository.countByUserId(userId);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should clear MongoDB collection before test")
    void testMongoDBCollectionCleanup() {
        // Act
        WorkoutSession savedSession = workoutSessionRepository.save(testSession);
        long countBefore = workoutSessionRepository.count();
        
        mongoTemplate.dropCollection("workout_sessions");
        long countAfter = workoutSessionRepository.count();

        // Assert
        assertThat(countBefore).isGreaterThan(0);
        assertThat(countAfter).isEqualTo(0);
    }
}