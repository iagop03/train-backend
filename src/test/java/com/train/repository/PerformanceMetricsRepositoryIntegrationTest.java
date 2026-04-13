package com.train.repository;

import com.train.config.TestContainerSpringBootTest;
import com.train.document.PerformanceMetrics;
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
@DisplayName("Integration Tests - PerformanceMetricsRepository with MongoDB")
class PerformanceMetricsRepositoryIntegrationTest {

    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    @Autowired
    private UserRepository userRepository;

    private PerformanceMetrics testMetrics;
    private Long userId;

    @BeforeEach
    void setUp() {
        // Create test user
        User user = userRepository.save(User.builder()
            .email("metrics@test.com")
            .username("metricsuser")
            .password("pass")
            .firstName("Metrics")
            .lastName("User")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());
        userId = user.getId();

        testMetrics = PerformanceMetrics.builder()
            .userId(userId)
            .exerciseName("Bench Press")
            .date(LocalDateTime.now())
            .weight(100.0)
            .reps(8)
            .sets(4)
            .totalVolume(3200.0)
            .maxWeight(100.0)
            .avgReps(8.0)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save performance metrics to MongoDB successfully")
    void testSavePerformanceMetrics() {
        // Act
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(testMetrics);

        // Assert
        assertThat(savedMetrics).isNotNull();
        assertThat(savedMetrics.getId()).isNotNull();
        assertThat(savedMetrics.getExerciseName()).isEqualTo("Bench Press");
        assertThat(savedMetrics.getTotalVolume()).isEqualTo(3200.0);
    }

    @Test
    @DisplayName("Should find metrics by user ID")
    void testFindMetricsByUserId() {
        // Arrange
        performanceMetricsRepository.save(testMetrics);
        PerformanceMetrics metrics2 = PerformanceMetrics.builder()
            .userId(userId)
            .exerciseName("Deadlift")
            .date(LocalDateTime.now())
            .weight(150.0)
            .reps(5)
            .sets(3)
            .totalVolume(2250.0)
            .maxWeight(150.0)
            .avgReps(5.0)
            .createdAt(LocalDateTime.now())
            .build();
        performanceMetricsRepository.save(metrics2);

        // Act
        List<PerformanceMetrics> metrics = performanceMetricsRepository.findByUserId(userId);

        // Assert
        assertThat(metrics).hasSize(2);
        assertThat(metrics).extracting(PerformanceMetrics::getExerciseName)
            .containsExactlyInAnyOrder("Bench Press", "Deadlift");
    }

    @Test
    @DisplayName("Should find metrics by exercise name")
    void testFindMetricsByExerciseName() {
        // Arrange
        performanceMetricsRepository.save(testMetrics);
        PerformanceMetrics metrics2 = PerformanceMetrics.builder()
            .userId(userId)
            .exerciseName("Bench Press")
            .date(LocalDateTime.now().minusDays(1))
            .weight(98.0)
            .reps(8)
            .sets(4)
            .totalVolume(3136.0)
            .maxWeight(100.0)
            .avgReps(8.0)
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
        performanceMetricsRepository.save(metrics2);

        // Act
        List<PerformanceMetrics> benchMetrics = performanceMetricsRepository
            .findByExerciseName("Bench Press");

        // Assert
        assertThat(benchMetrics).hasSizeGreaterThanOrEqualTo(2);
        assertThat(benchMetrics).allMatch(m -> "Bench Press".equals(m.getExerciseName()));
    }

    @Test
    @DisplayName("Should find metrics by user and exercise")
    void testFindMetricsByUserAndExercise() {
        // Arrange
        performanceMetricsRepository.save(testMetrics);

        // Act
        List<PerformanceMetrics> metrics = performanceMetricsRepository
            .findByUserIdAndExerciseName(userId, "Bench Press");

        // Assert
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).getExerciseName()).isEqualTo("Bench Press");
    }

    @Test
    @DisplayName("Should find metrics by date range")
    void testFindMetricsByDateRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        performanceMetricsRepository.save(testMetrics);
        
        PerformanceMetrics oldMetrics = PerformanceMetrics.builder()
            .userId(userId)
            .exerciseName("Bench Press")
            .date(now.minusDays(10))
            .weight(95.0)
            .reps(8)
            .sets(4)
            .createdAt(now.minusDays(10))
            .build();
        performanceMetricsRepository.save(oldMetrics);

        // Act
        List<PerformanceMetrics> recentMetrics = performanceMetricsRepository
            .findByUserIdAndDateBetween(userId, now.minusDays(5), now.plusDays(1));

        // Assert
        assertThat(recentMetrics).hasSize(1);
        assertThat(recentMetrics.get(0).getDate()).isCloseTo(now, within(
            java.time.temporal.ChronoUnit.MINUTES, 1));
    }

    @Test
    @DisplayName("Should find metrics by ID")
    void testFindMetricsById() {
        // Arrange
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(testMetrics);

        // Act
        Optional<PerformanceMetrics> foundMetrics = performanceMetricsRepository
            .findById(savedMetrics.getId());

        // Assert
        assertThat(foundMetrics).isPresent();
        assertThat(foundMetrics.get().getExerciseName()).isEqualTo("Bench Press");
    }

    @Test
    @DisplayName("Should update performance metrics")
    void testUpdatePerformanceMetrics() {
        // Arrange
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(testMetrics);

        // Act
        savedMetrics.setWeight(110.0);
        savedMetrics.setTotalVolume(3520.0);
        PerformanceMetrics updatedMetrics = performanceMetricsRepository.save(savedMetrics);

        // Assert
        assertThat(updatedMetrics.getWeight()).isEqualTo(110.0);
        assertThat(updatedMetrics.getTotalVolume()).isEqualTo(3520.0);
    }

    @Test
    @DisplayName("Should delete performance metrics")
    void testDeletePerformanceMetrics() {
        // Arrange
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(testMetrics);
        String metricsId = savedMetrics.getId();

        // Act
        performanceMetricsRepository.deleteById(metricsId);
        Optional<PerformanceMetrics> deletedMetrics = performanceMetricsRepository
            .findById(metricsId);

        // Assert
        assertThat(deletedMetrics).isEmpty();
    }

    @Test
    @DisplayName("Should find max weight for exercise")
    void testFindMaxWeightForExercise() {
        // Arrange
        performanceMetricsRepository.save(testMetrics);
        PerformanceMetrics metrics2 = PerformanceMetrics.builder()
            .userId(userId)
            .exerciseName("Bench Press")
            .date(LocalDateTime.now().minusDays(1))
            .weight(105.0)
            .reps(6)
            .sets(4)
            .maxWeight(105.0)
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
        performanceMetricsRepository.save(metrics2);

        // Act
        List<PerformanceMetrics> metrics = performanceMetricsRepository
            .findByUserIdAndExerciseName(userId, "Bench Press");
        Double maxWeight = metrics.stream()
            .mapToDouble(PerformanceMetrics::getWeight)
            .max()
            .orElse(0.0);

        // Assert
        assertThat(maxWeight).isEqualTo(105.0);
    }
}