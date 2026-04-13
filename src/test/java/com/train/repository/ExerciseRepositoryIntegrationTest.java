package com.train.repository;

import com.train.config.TestContainerSpringBootTest;
import com.train.entity.Exercise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@TestContainerSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Integration Tests - ExerciseRepository with PostgreSQL")
class ExerciseRepositoryIntegrationTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    private Exercise testExercise;
    private Long userId;

    @BeforeEach
    void setUp() {
        // Create test user first
        User user = userRepository.save(User.builder()
            .email("exercise@test.com")
            .username("exerciseuser")
            .password("pass")
            .firstName("Ex")
            .lastName("User")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());
        userId = user.getId();

        testExercise = Exercise.builder()
            .userId(userId)
            .name("Bench Press")
            .category("Chest")
            .description("Barbell Bench Press")
            .sets(4)
            .reps(8)
            .weight(100.0)
            .duration(45)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save exercise to PostgreSQL successfully")
    void testSaveExercise() {
        // Act
        Exercise savedExercise = exerciseRepository.save(testExercise);

        // Assert
        assertThat(savedExercise).isNotNull();
        assertThat(savedExercise.getId()).isNotNull();
        assertThat(savedExercise.getName()).isEqualTo("Bench Press");
        assertThat(savedExercise.getCategory()).isEqualTo("Chest");
    }

    @Test
    @DisplayName("Should find exercises by user ID")
    void testFindExercisesByUserId() {
        // Arrange
        exerciseRepository.save(testExercise);
        Exercise exercise2 = Exercise.builder()
            .userId(userId)
            .name("Deadlift")
            .category("Back")
            .description("Barbell Deadlift")
            .sets(3)
            .reps(5)
            .weight(150.0)
            .createdAt(LocalDateTime.now())
            .build();
        exerciseRepository.save(exercise2);

        // Act
        List<Exercise> exercises = exerciseRepository.findByUserId(userId);

        // Assert
        assertThat(exercises).hasSize(2);
        assertThat(exercises).extracting(Exercise::getName)
            .containsExactlyInAnyOrder("Bench Press", "Deadlift");
    }

    @Test
    @DisplayName("Should find exercises by category")
    void testFindExercisesByCategory() {
        // Arrange
        exerciseRepository.save(testExercise);
        Exercise exercise2 = Exercise.builder()
            .userId(userId)
            .name("Incline Press")
            .category("Chest")
            .description("Incline Bench Press")
            .sets(4)
            .reps(10)
            .weight(80.0)
            .createdAt(LocalDateTime.now())
            .build();
        exerciseRepository.save(exercise2);

        // Act
        List<Exercise> chestExercises = exerciseRepository.findByCategory("Chest");

        // Assert
        assertThat(chestExercises).hasSize(2);
        assertThat(chestExercises).allMatch(ex -> "Chest".equals(ex.getCategory()));
    }

    @Test
    @DisplayName("Should find exercise by ID")
    void testFindExerciseById() {
        // Arrange
        Exercise savedExercise = exerciseRepository.save(testExercise);

        // Act
        Optional<Exercise> foundExercise = exerciseRepository.findById(savedExercise.getId());

        // Assert
        assertThat(foundExercise).isPresent();
        assertThat(foundExercise.get().getName()).isEqualTo("Bench Press");
    }

    @Test
    @DisplayName("Should update exercise")
    void testUpdateExercise() {
        // Arrange
        Exercise savedExercise = exerciseRepository.save(testExercise);

        // Act
        savedExercise.setWeight(120.0);
        savedExercise.setReps(10);
        Exercise updatedExercise = exerciseRepository.save(savedExercise);

        // Assert
        assertThat(updatedExercise.getWeight()).isEqualTo(120.0);
        assertThat(updatedExercise.getReps()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should delete exercise")
    void testDeleteExercise() {
        // Arrange
        Exercise savedExercise = exerciseRepository.save(testExercise);
        Long exerciseId = savedExercise.getId();

        // Act
        exerciseRepository.deleteById(exerciseId);
        Optional<Exercise> deletedExercise = exerciseRepository.findById(exerciseId);

        // Assert
        assertThat(deletedExercise).isEmpty();
    }

    @Test
    @DisplayName("Should count exercises by user ID")
    void testCountByUserId() {
        // Arrange
        exerciseRepository.save(testExercise);
        exerciseRepository.save(testExercise);
        exerciseRepository.save(testExercise);

        // Act
        long count = exerciseRepository.countByUserId(userId);

        // Assert
        assertThat(count).isEqualTo(3);
    }
}