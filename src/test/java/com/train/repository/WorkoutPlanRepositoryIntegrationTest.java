package com.train.repository;

import com.train.config.TestContainerSpringBootTest;
import com.train.document.WorkoutPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@TestContainerSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Integration Tests - WorkoutPlanRepository with MongoDB")
class WorkoutPlanRepositoryIntegrationTest {

    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    private UserRepository userRepository;

    private WorkoutPlan testPlan;
    private Long userId;

    @BeforeEach
    void setUp() {
        // Create test user
        User user = userRepository.save(User.builder()
            .email("plan@test.com")
            .username("planuser")
            .password("pass")
            .firstName("Plan")
            .lastName("User")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());
        userId = user.getId();

        testPlan = WorkoutPlan.builder()
            .userId(userId)
            .planName("Strength Training")
            .description("4-day strength training program")
            .daysPerWeek(4)
            .goal("Muscle Gain")
            .difficulty("Intermediate")
            .durationWeeks(12)
            .exercisesPerSession(5)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save workout plan to MongoDB successfully")
    void testSaveWorkoutPlan() {
        // Act
        WorkoutPlan savedPlan = workoutPlanRepository.save(testPlan);

        // Assert
        assertThat(savedPlan).isNotNull();
        assertThat(savedPlan.getId()).isNotNull();
        assertThat(savedPlan.getPlanName()).isEqualTo("Strength Training");
        assertThat(savedPlan.getDaysPerWeek()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find workout plans by user ID")
    void testFindPlansByUserId() {
        // Arrange
        workoutPlanRepository.save(testPlan);
        WorkoutPlan plan2 = WorkoutPlan.builder()
            .userId(userId)
            .planName("Cardio Training")
            .description("3-day cardio program")
            .daysPerWeek(3)
            .goal("Weight Loss")
            .difficulty("Beginner")
            .durationWeeks(8)
            .exercisesPerSession(4)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
        workoutPlanRepository.save(plan2);

        // Act
        List<WorkoutPlan> plans = workoutPlanRepository.findByUserId(userId);

        // Assert
        assertThat(plans).hasSize(2);
        assertThat(plans).extracting(WorkoutPlan::getPlanName)
            .containsExactlyInAnyOrder("Strength Training", "Cardio Training");
    }

    @Test
    @DisplayName("Should find active workout plans")
    void testFindActivePlans() {
        // Arrange
        workoutPlanRepository.save(testPlan);
        WorkoutPlan inactivePlan = WorkoutPlan.builder()
            .userId(userId)
            .planName("Old Plan")
            .description("Inactive plan")
            .daysPerWeek(5)
            .isActive(false)
            .createdAt(LocalDateTime.now())
            .build();
        workoutPlanRepository.save(inactivePlan);

        // Act
        List<WorkoutPlan> activePlans = workoutPlanRepository.findByIsActive(true);

        // Assert
        assertThat(activePlans).hasSize(1);
        assertThat(activePlans.get(0).getPlanName()).isEqualTo("Strength Training");
    }

    @Test
    @DisplayName("Should find plans by goal")
    void testFindPlansByGoal() {
        // Arrange
        workoutPlanRepository.save(testPlan);
        WorkoutPlan gainPlan = WorkoutPlan.builder()
            .userId(userId)
            .planName("Another Gain Plan")
            .goal("Muscle Gain")
            .daysPerWeek(4)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
        workoutPlanRepository.save(gainPlan);

        // Act
        List<WorkoutPlan> gainPlans = workoutPlanRepository.findByGoal("Muscle Gain");

        // Assert
        assertThat(gainPlans).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gainPlans).allMatch(plan -> "Muscle Gain".equals(plan.getGoal()));
    }

    @Test
    @DisplayName("Should find plan by ID")
    void testFindPlanById() {
        // Arrange
        WorkoutPlan savedPlan = workoutPlanRepository.save(testPlan);

        // Act
        Optional<WorkoutPlan> foundPlan = workoutPlanRepository.findById(savedPlan.getId());

        // Assert
        assertThat(foundPlan).isPresent();
        assertThat(foundPlan.get().getPlanName()).isEqualTo("Strength Training");
    }

    @Test
    @DisplayName("Should update workout plan")
    void testUpdateWorkoutPlan() {
        // Arrange
        WorkoutPlan savedPlan = workoutPlanRepository.save(testPlan);

        // Act
        savedPlan.setDurationWeeks(16);
        savedPlan.setDaysPerWeek(5);
        WorkoutPlan updatedPlan = workoutPlanRepository.save(savedPlan);

        // Assert
        assertThat(updatedPlan.getDurationWeeks()).isEqualTo(16);
        assertThat(updatedPlan.getDaysPerWeek()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should delete workout plan")
    void testDeleteWorkoutPlan() {
        // Arrange
        WorkoutPlan savedPlan = workoutPlanRepository.save(testPlan);
        String planId = savedPlan.getId();

        // Act
        workoutPlanRepository.deleteById(planId);
        Optional<WorkoutPlan> deletedPlan = workoutPlanRepository.findById(planId);

        // Assert
        assertThat(deletedPlan).isEmpty();
    }

    @Test
    @DisplayName("Should find plans by difficulty")
    void testFindPlansByDifficulty() {
        // Arrange
        workoutPlanRepository.save(testPlan); // Intermediate
        WorkoutPlan advancedPlan = WorkoutPlan.builder()
            .userId(userId)
            .planName("Advanced Plan")
            .difficulty("Advanced")
            .daysPerWeek(6)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
        workoutPlanRepository.save(advancedPlan);

        // Act
        List<WorkoutPlan> intermediatePlans = workoutPlanRepository.findByDifficulty("Intermediate");

        // Assert
        assertThat(intermediatePlans).hasSize(1);
        assertThat(intermediatePlans.get(0).getPlanName()).isEqualTo("Strength Training");
    }

    @Test
    @DisplayName("Should find user's active plans")
    void testFindUserActivePlans() {
        // Arrange
        workoutPlanRepository.save(testPlan);
        WorkoutPlan inactivePlan = WorkoutPlan.builder()
            .userId(userId)
            .planName("Inactive")
            .isActive(false)
            .createdAt(LocalDateTime.now())
            .build();
        workoutPlanRepository.save(inactivePlan);

        // Act
        List<WorkoutPlan> userActivePlans = workoutPlanRepository.findByUserIdAndIsActive(userId, true);

        // Assert
        assertThat(userActivePlans).hasSize(1);
        assertThat(userActivePlans.get(0).getPlanName()).isEqualTo("Strength Training");
    }
}